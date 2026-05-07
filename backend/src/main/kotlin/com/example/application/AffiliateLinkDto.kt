package com.example.application

import java.math.BigDecimal

data class AffiliateLinkDto(
    val url: String,
    val trackingId: String,
    val platform: String,
    val productName: String,
    val price: BigDecimal,
)
