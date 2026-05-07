package com.example.presentation.dto

import java.math.BigDecimal

data class GenerateArticleResponse(
    val articleId: String,
    val title: String,
    val content: String,
    val imageUrl: String,
    val affiliateLinks: List<AffiliateLinkResponse>,
    val status: String,
)

data class AffiliateLinkResponse(
    val url: String,
    val trackingId: String,
    val platform: String,
    val productName: String,
    val price: BigDecimal,
)
