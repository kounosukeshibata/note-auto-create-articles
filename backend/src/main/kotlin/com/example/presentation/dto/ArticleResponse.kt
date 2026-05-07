package com.example.presentation.dto

import java.math.BigDecimal

data class ArticleResponse(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String,
    val imageAltText: String,
    val keywords: List<String>,
    val affiliateLinks: List<AffiliateLinkResponse>,
    val status: String,
)
