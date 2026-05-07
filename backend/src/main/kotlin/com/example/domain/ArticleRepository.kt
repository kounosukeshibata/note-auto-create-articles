package com.example.domain

interface ArticleRepository {
    fun save(article: Article): Article
    fun findById(id: ArticleId): Article?
    fun findAllByUserId(userId: UserId): List<Article>
    fun delete(id: ArticleId)
}
