package com.example.infrastructure

import com.example.domain.AffiliatePlatform
import com.example.domain.ProductInfo
import com.example.domain.SeoKeyword
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
@ConditionalOnProperty("amazon.stub", havingValue = "true", matchIfMissing = true)
class StubAmazonAffiliateClient(
    @Value("\${amazon.partner-tag:}") private val partnerTag: String,
) : AffiliateApiClient {

    override val platform = AffiliatePlatform.AMAZON

    override fun searchProducts(keywords: List<SeoKeyword>): List<ProductInfo> {
        val query = keywords.take(3).joinToString(" ") { it.value }
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8)
        val tagSuffix = if (partnerTag.isNotBlank()) "&tag=$partnerTag" else ""

        return listOf(
            buildProduct(query, "1位", 19800, 0.08, encoded, tagSuffix),
            buildProduct(query, "2位", 14800, 0.07, encoded, tagSuffix),
            buildProduct(query, "3位", 9980, 0.06, encoded, tagSuffix),
            buildProduct(query, "4位", 24800, 0.09, encoded, tagSuffix),
            buildProduct(query, "5位", 5980, 0.05, encoded, tagSuffix),
        )
    }

    private fun buildProduct(
        query: String,
        rank: String,
        price: Int,
        rate: Double,
        encoded: String,
        tagSuffix: String,
    ) = ProductInfo(
        name = "$query おすすめ$rank",
        price = BigDecimal(price),
        category = "general",
        thumbnailUrl = "https://images-na.ssl-images-amazon.com/images/I/placeholder.jpg",
        commissionRate = rate,
        affiliateUrl = "https://www.amazon.co.jp/s?k=$encoded$tagSuffix",
    )
}
