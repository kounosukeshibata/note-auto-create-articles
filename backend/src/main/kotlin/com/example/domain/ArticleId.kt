package com.example.domain

import java.util.UUID

@JvmInline
value class ArticleId(val value: UUID) {
    companion object {
        fun generate(): ArticleId = ArticleId(UUID.randomUUID())
    }
}
