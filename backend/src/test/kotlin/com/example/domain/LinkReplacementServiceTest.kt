package com.example.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class LinkReplacementServiceTest {

    private val service = LinkReplacementService()

    private fun createLink(index: Int, url: String = "https://example.com/product/$index"): AffiliateLink =
        AffiliateLink(
            url = url,
            trackingId = "tracking-$index",
            platform = AffiliatePlatform.AMAZON,
            productInfo = ProductInfo(
                name = "商品$index",
                price = BigDecimal("1000"),
                category = "test",
                thumbnailUrl = "https://example.com/thumb/$index",
                commissionRate = 0.05,
            ),
        )

    @Test
    fun `placeholders should be replaced with affiliate URLs`() {
        val links = ProductLinks().add(createLink(0)).add(createLink(1))
        val content = Content(
            title = "テスト記事",
            text = "最初の商品: {{product_link_0}}, 次の商品: {{product_link_1}}",
        )

        val result = service.replace(content, links)

        assertEquals(
            "最初の商品: https://example.com/product/0, 次の商品: https://example.com/product/1",
            result.text,
        )
    }

    @Test
    fun `placeholders exceeding product count should remain unchanged`() {
        val links = ProductLinks().add(createLink(0))
        val content = Content(
            title = "テスト記事",
            text = "商品0: {{product_link_0}}, 商品1: {{product_link_1}}",
        )

        val result = service.replace(content, links)

        assertEquals(
            "商品0: https://example.com/product/0, 商品1: {{product_link_1}}",
            result.text,
        )
    }

    @Test
    fun `text should not change when there are no products`() {
        val links = ProductLinks()
        val originalText = "商品はこちら: {{product_link_0}}"
        val content = Content(title = "テスト記事", text = originalText)

        val result = service.replace(content, links)

        assertEquals(originalText, result.text)
    }
}
