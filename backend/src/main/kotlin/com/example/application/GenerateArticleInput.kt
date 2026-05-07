package com.example.application

import com.example.domain.AffiliatePlatform
import com.example.domain.UserId

data class GenerateArticleInput(
    val theme: String,
    val affiliatePlatforms: List<AffiliatePlatform>,
    val userId: UserId,
    val targetPainPoint: String = "",
    val targetIdealState: String = "",
    val storyTrigger: String = "",
    val uniqueInsight: String = "",
    val articleType: String = "一般",
    val ctaInfo: String = "",
    val wordCount: Int? = null,
)
