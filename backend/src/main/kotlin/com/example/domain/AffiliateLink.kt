package com.example.domain

data class AffiliateLink(
    val url: String,
    val trackingId: String,
    val platform: AffiliatePlatform,
    val productInfo: ProductInfo,
)
