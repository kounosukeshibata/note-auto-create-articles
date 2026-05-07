package com.example.infrastructure.persistence

data class AffiliateLinkJson(
    val url: String = "",
    val trackingId: String = "",
    val platform: String = "",
    val productName: String = "",
    val price: String = "0",
    val category: String = "",
    val thumbnailUrl: String = "",
    val commissionRate: Double = 0.0,
)
