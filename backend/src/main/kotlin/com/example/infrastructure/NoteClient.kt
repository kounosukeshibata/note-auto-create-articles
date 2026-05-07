package com.example.infrastructure

import com.example.domain.Article

data class NotePostResult(val success: Boolean, val noteUrl: String? = null)

interface NoteClient {
    fun postDraft(article: Article): NotePostResult
}
