package com.example.infrastructure

import com.example.domain.Content
import com.example.domain.Image
import com.example.domain.ProductInfo
import com.example.domain.SeoKeyword
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

@Component
@ConditionalOnProperty("vertex.ai.stub", havingValue = "false")
class RealVertexAiClient(
    @Value("\${gemini.api-key}") private val geminiApiKey: String,
) : VertexAiClient {

    private val logger = LoggerFactory.getLogger(RealVertexAiClient::class.java)
    private val mapper = ObjectMapper()
    private val contentTemplate = PromptLoader.load("prompts/vertex/content-generation.md")
    private val keywordTemplate = PromptLoader.load("prompts/vertex/keyword-extraction.md")

    override fun extractKeywords(theme: String): List<SeoKeyword> {
        val prompt = keywordTemplate.replace("{theme}", theme)
        val text = callGemini(prompt, maxTokens = 256, temperature = 0.2) ?: return fallbackKeywords(theme)
        return try {
            val jsonStart = text.indexOf('[')
            val jsonEnd = text.lastIndexOf(']')
            if (jsonStart < 0 || jsonEnd < 0) return fallbackKeywords(theme)
            val list = mapper.readValue(
                text.substring(jsonStart, jsonEnd + 1),
                mapper.typeFactory.constructCollectionType(List::class.java, String::class.java),
            ) as List<String>
            list.filter { it.isNotBlank() }.map { SeoKeyword(it) }
        } catch (e: Exception) {
            logger.warn("キーワードパース失敗: ${e.message}")
            fallbackKeywords(theme)
        }
    }

    override fun generateContent(
        theme: String,
        keywords: List<SeoKeyword>,
        products: List<ProductInfo>,
        targetPainPoint: String,
        targetIdealState: String,
        storyTrigger: String,
        uniqueInsight: String,
        articleType: String,
        ctaInfo: String,
        wordCount: Int?,
    ): Content {
        val keywordStr = keywords.joinToString("・") { it.value }
        val productsSection = buildProductsSection(products)
        val wordCountInstruction = wordCount?.let { "記事全体の文字数は${it}文字程度になるよう調整してください。\n\n" } ?: ""

        val prompt = wordCountInstruction + contentTemplate
            .replace("{theme}", theme)
            .replace("{keywords}", keywordStr)
            .replace("{products_section}", productsSection)
            .replace("{target_pain_point}", targetPainPoint.ifBlank { "（未入力）" })
            .replace("{target_ideal_state}", targetIdealState.ifBlank { "（未入力）" })
            .replace("{story_trigger}", storyTrigger.ifBlank { "（未入力）" })
            .replace("{unique_insight}", uniqueInsight.ifBlank { "（未入力）" })
            .replace("{article_type}", articleType)
            .replace("{cta_info}", ctaInfo.ifBlank { "（未入力）" })

        val maxTokens = when {
            wordCount != null && wordCount >= 4000 -> 12000
            wordCount != null && wordCount >= 3000 -> 9000
            wordCount != null && wordCount >= 2000 -> 6000
            else -> 4000
        }

        val generatedText = callGemini(prompt, maxTokens = maxTokens, temperature = 0.7)
            ?: return Content(title = "${theme}の完全ガイド", text = "記事の生成に失敗しました。")

        return parseContent(generatedText, theme)
    }

    override fun generateImage(prompt: String): Image =
        Image(
            url = "https://placehold.co/1280x670/1E88E5/FFFFFF?text=AI+Generated",
            altText = prompt,
        )

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun buildProductsSection(products: List<ProductInfo>): String {
        if (products.isEmpty()) return "（紹介商品なし）"
        return products.mapIndexed { i, p ->
            "【第${i + 1}位】 商品名: ${p.name} / 価格: ${p.price.toPlainString()}円 / リンクプレースホルダー: {{product_link_$i}}"
        }.joinToString("\n")
    }

    private fun parseContent(text: String, theme: String): Content {
        val lines = text.lines()
        // [タイトル] の次の非空行をタイトルとして取得
        val titleLineIndex = lines.indexOfFirst { it.trimStart().startsWith("[タイトル]") }
        val title = if (titleLineIndex >= 0) {
            // 同じ行に値がある場合 ("[ タイトル] 実際のタイトル") と次行にある場合の両方に対応
            val sameLine = lines[titleLineIndex].replace(Regex("\\[タイトル\\]\\s*(?:※[^\\n]*)?"), "").trim()
            if (sameLine.isNotBlank()) sameLine
            else lines.drop(titleLineIndex + 1).firstOrNull { it.isNotBlank() }?.trim() ?: "${theme}の完全ガイド"
        } else {
            "${theme}の完全ガイド"
        }
        return Content(title = title, text = text)
    }

    private fun fallbackKeywords(theme: String) =
        listOf(SeoKeyword("おすすめ"), SeoKeyword(theme.take(20)), SeoKeyword("比較"), SeoKeyword("人気"))

    // ── Gemini API ────────────────────────────────────────────────────────────

    private fun callGemini(prompt: String, maxTokens: Int, temperature: Double): String? {
        val requestBody = mapper.writeValueAsString(
            mapOf(
                "contents" to listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))),
                "generationConfig" to mapOf(
                    "temperature" to temperature,
                    "maxOutputTokens" to maxTokens,
                    "thinkingConfig" to mapOf("thinkingBudget" to 0),
                ),
            ),
        )

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$geminiApiKey"
        val conn = URI.create(url).toURL().openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 15_000
        conn.readTimeout = 120_000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.outputStream.use { it.write(requestBody.toByteArray(StandardCharsets.UTF_8)) }

        val status = conn.responseCode
        if (status !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "no body"
            logger.error("Gemini API エラー ($status): $err")
            return null
        }

        val responseText = conn.inputStream.bufferedReader().readText()
        return extractText(responseText)
    }

    private fun extractText(json: String): String? {
        val root = mapper.readTree(json)
        val parts = root.path("candidates").firstOrNull()?.path("content")?.path("parts")
            ?: return null
        return parts.asSequence()
            .filter { !it.path("thought").asBoolean(false) }
            .mapNotNull { it.path("text").asText(null)?.takeIf { t -> t.isNotBlank() } }
            .firstOrNull()
    }
}
