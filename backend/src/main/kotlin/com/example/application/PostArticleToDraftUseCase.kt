package com.example.application

import com.example.domain.UserId

interface PostArticleToDraftUseCase {
    fun execute(articleId: String, userId: UserId): PostToDraftOutput
}

data class PostToDraftOutput(val noteUrl: String)
