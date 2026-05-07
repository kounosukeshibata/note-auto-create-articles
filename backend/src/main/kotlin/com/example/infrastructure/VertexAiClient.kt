package com.example.infrastructure

import com.example.domain.Content
import com.example.domain.Image
import com.example.domain.ProductInfo
import com.example.domain.SeoKeyword

interface VertexAiClient {
    fun extractKeywords(theme: String): List<SeoKeyword>
    fun generateContent(
        theme: String,
        keywords: List<SeoKeyword>,
        products: List<ProductInfo>,
        targetPainPoint: String = "",
        targetIdealState: String = "",
        storyTrigger: String = "",
        uniqueInsight: String = "",
        articleType: String = "一般",
        ctaInfo: String = "",
        wordCount: Int? = null,
    ): Content
    fun generateImage(prompt: String): Image
}
