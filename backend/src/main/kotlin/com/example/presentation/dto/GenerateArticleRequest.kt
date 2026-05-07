package com.example.presentation.dto

import jakarta.validation.constraints.NotBlank

data class GenerateArticleRequest(
    @field:NotBlank val theme: String,
    val affiliatePlatforms: List<String> = listOf("AMAZON"),
    val targetPainPoint: String? = null,
    val targetIdealState: String? = null,
    val storyTrigger: String? = null,
    val uniqueInsight: String? = null,
    val articleType: String? = "一般",
    val ctaInfo: String? = null,
)
