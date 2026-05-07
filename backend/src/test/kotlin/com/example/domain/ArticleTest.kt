package com.example.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class ArticleTest {

    private fun createArticle(
        text: String = "テスト記事本文",
        productLinks: ProductLinks = ProductLinks(),
        status: ArticleStatus = ArticleStatus.GENERATED,
    ): Article = Article(
        id = ArticleId(UUID.randomUUID()),
        userId = UserId(UUID.randomUUID()),
        content = Content(title = "テストタイトル", text = text),
        image = Image(url = "https://example.com/image.png", altText = "テスト画像"),
        keywords = listOf(SeoKeyword("テスト")),
        productLinks = productLinks,
        status = status,
    )

    @Test
    fun `markAsSaved should change status to SAVED`() {
        val article = createArticle()
        val saved = article.markAsSaved()
        assertEquals(ArticleStatus.SAVED, saved.status)
    }

    @Test
    fun `markAsDrafted should change status to NOTE_DRAFTED`() {
        val article = createArticle()
        val drafted = article.markAsDrafted()
        assertEquals(ArticleStatus.NOTE_DRAFTED, drafted.status)
    }

    @Test
    fun `injectLinks should replace placeholders with affiliate URLs`() {
        val link = AffiliateLink(
            url = "https://affiliate.example.com/product/1",
            trackingId = "tracking-001",
            platform = AffiliatePlatform.AMAZON,
            productInfo = ProductInfo(
                name = "テスト商品",
                price = BigDecimal("9800"),
                category = "electronics",
                thumbnailUrl = "https://example.com/thumb.png",
                commissionRate = 0.05,
            ),
        )
        val productLinks = ProductLinks().add(link)
        val article = createArticle(
            text = "商品はこちら: {{product_link_0}} をご覧ください",
            productLinks = productLinks,
        )

        val service = LinkReplacementService()
        val result = article.injectLinks(service)

        assertEquals(
            "商品はこちら: https://affiliate.example.com/product/1 をご覧ください",
            result.content.text,
        )
    }
}
