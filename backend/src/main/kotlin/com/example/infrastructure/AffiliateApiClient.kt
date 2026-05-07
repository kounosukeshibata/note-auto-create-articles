package com.example.infrastructure

import com.example.domain.AffiliatePlatform
import com.example.domain.ProductInfo
import com.example.domain.SeoKeyword

interface AffiliateApiClient {
    val platform: AffiliatePlatform
    fun searchProducts(keywords: List<SeoKeyword>): List<ProductInfo>
}
