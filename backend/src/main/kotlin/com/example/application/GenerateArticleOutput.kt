package com.example.application

import java.math.BigDecimal

data class GenerateArticleOutput(
    val articleId: String,
    val title: String,
    val content: String,
    val imageUrl: String,
    val affiliateLinks: List<AffiliateLinkDto>,
    val status: String,
)
