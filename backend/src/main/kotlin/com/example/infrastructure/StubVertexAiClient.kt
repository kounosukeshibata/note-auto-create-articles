package com.example.infrastructure

import com.example.domain.Content
import com.example.domain.Image
import com.example.domain.ProductInfo
import com.example.domain.SeoKeyword
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("vertex.ai.stub", havingValue = "true", matchIfMissing = true)
class StubVertexAiClient : VertexAiClient {

    private val contentTemplate = PromptLoader.load("prompts/vertex/content-generation.md")
    private val productItemTemplate = PromptLoader.load("prompts/vertex/content-generation-product-item.md")

    override fun extractKeywords(theme: String): List<SeoKeyword> =
        listOf(SeoKeyword("おすすめ"), SeoKeyword(theme.take(20)), SeoKeyword("比較"), SeoKeyword("人気"))

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

        val productsSection = if (products.isNotEmpty()) {
            "## おすすめ商品紹介\n\n" + products.mapIndexed { i, product ->
                productItemTemplate
                    .replace("{rank}", "${i + 1}")
                    .replace("{name}", product.name)
                    .replace("{price}", product.price.toPlainString())
                    .replace("{index}", "$i")
                    .replace("{theme}", theme)
            }.joinToString("\n\n")
        } else {
            "## 商品の活用方法\n\n${theme}を最大限に活用するには、まず基本的な使い方を理解することが重要です。" +
                "日常的に取り入れることで、生活の質を向上させることができます。"
        }

        val text = contentTemplate
            .replace("{theme}", theme)
            .replace("{keywords}", keywordStr)
            .replace("{products_section}", productsSection)
            .replace("{target_pain_point}", targetPainPoint)
            .replace("{target_ideal_state}", targetIdealState)
            .replace("{story_trigger}", storyTrigger)
            .replace("{unique_insight}", uniqueInsight)
            .replace("{article_type}", articleType)
            .replace("{cta_info}", ctaInfo)

        return Content(title = "${theme}の完全ガイド", text = text)
    }

    override fun generateImage(prompt: String): Image =
        Image(
            url = "https://placehold.co/1280x670/1E88E5/FFFFFF?text=AI+Generated+Image",
            altText = prompt,
        )
}
