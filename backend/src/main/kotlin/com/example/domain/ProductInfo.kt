package com.example.domain

import java.math.BigDecimal

data class ProductInfo(
    val name: String,
    val price: BigDecimal,
    val category: String,
    val thumbnailUrl: String,
    val commissionRate: Double = 0.0,
    val affiliateUrl: String = "",
)
