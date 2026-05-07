package com.example.infrastructure

import com.example.domain.AffiliatePlatform
import com.example.domain.ProductInfo
import com.example.domain.SeoKeyword
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


/**
 * Google AI Gemini API を使ってテーマに合った実在 Amazon 商品を提案し、
 * アソシエイトタグ付きの検索 URL を生成するアフィリエイトクライアント。
 * PA API 認証情報不要。Cloud Run サービスアカウントの権限で動作する。
 */
@Component
@ConditionalOnProperty("amazon.stub", havingValue = "false")
class RealAmazonAffiliateClient(
    @Value("\${amazon.partner-tag}") private val partnerTag: String,
    @Value("\${gemini.api-key}") private val geminiApiKey: String,
) : AffiliateApiClient {

    private val logger = LoggerFactory.getLogger(RealAmazonAffiliateClient::class.java)
    private val mapper = ObjectMapper()
    private val promptTemplate: String = PromptLoader.load("prompts/gemini/amazon-product-suggestion.md")

    override val platform = AffiliatePlatform.AMAZON

    override fun searchProducts(keywords: List<SeoKeyword>): List<ProductInfo> {
        val theme = keywords.joinToString("、") { it.value }
        return try {
            val suggestions = askGeminiForProducts(theme)
            if (suggestions.isEmpty()) throw RuntimeException("Gemini から商品提案が返されませんでした")
            suggestions.map { s ->
                val encoded = URLEncoder.encode(s.keyword, StandardCharsets.UTF_8)
                ProductInfo(
                    name = s.name,
                    price = BigDecimal(s.price.coerceAtLeast(100)),
                    category = "general",
                    thumbnailUrl = "",
                    commissionRate = 0.08,
                    affiliateUrl = "https://www.amazon.co.jp/s?k=$encoded&tag=$partnerTag",
                )
            }
        } catch (e: Exception) {
            logger.error("Gemini 商品提案失敗: ${e.message}", e)
            throw e
        }
    }

    // ── Gemini API call ───────────────────────────────────────────────────────

    private fun askGeminiForProducts(theme: String): List<ProductSuggestion> {
        val prompt = promptTemplate.replace("{theme}", theme)

        val requestBody = mapper.writeValueAsString(
            mapOf(
                "contents" to listOf(
                    mapOf("parts" to listOf(mapOf("text" to prompt)))
                ),
                "generationConfig" to mapOf(
                    "temperature" to 0.2,
                    "maxOutputTokens" to 1024,
                    "thinkingConfig" to mapOf("thinkingBudget" to 0),
                )
            )
        )

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$geminiApiKey"
        val conn = URI.create(url).toURL().openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000
        conn.setRequestProperty("Content-Type", "application/json")

        conn.outputStream.use { it.write(requestBody.toByteArray(StandardCharsets.UTF_8)) }

        val status = conn.responseCode
        if (status !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "no body"
            throw RuntimeException("Gemini API エラー ($status): $err")
        }

        val responseText = conn.inputStream.bufferedReader().readText()
        return parseGeminiResponse(responseText)
    }

    private fun parseGeminiResponse(json: String): List<ProductSuggestion> {
        val root = mapper.readTree(json)
        val parts = root.path("candidates").firstOrNull()?.path("content")?.path("parts")
            ?: return emptyList()

        // thinking モードでは thought=true の part が先頭に来るためスキップし、実テキストを取得
        val text = parts.asSequence()
            .filter { !it.path("thought").asBoolean(false) }
            .mapNotNull { it.path("text").asText(null)?.takeIf { t -> t.isNotBlank() } }
            .firstOrNull() ?: return emptyList()

        logger.debug("Gemini レスポンステキスト: ${text.take(200)}")

        val jsonStart = text.indexOf('[')
        val jsonEnd = text.lastIndexOf(']')
        if (jsonStart < 0 || jsonEnd < 0) {
            logger.warn("商品リスト JSON が見つかりません: ${text.take(200)}")
            return emptyList()
        }

        return try {
            mapper.readValue(
                text.substring(jsonStart, jsonEnd + 1),
                mapper.typeFactory.constructCollectionType(List::class.java, ProductSuggestion::class.java)
            )
        } catch (e: Exception) {
            logger.warn("商品リストのパース失敗: ${e.message} / テキスト: ${text.take(200)}")
            emptyList()
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ProductSuggestion(
        val name: String = "",
        val price: Int = 0,
        val keyword: String = "",
    )
}
