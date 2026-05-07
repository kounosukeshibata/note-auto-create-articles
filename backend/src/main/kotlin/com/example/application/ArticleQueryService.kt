package com.example.application

import com.example.domain.Article
import com.example.domain.ArticleId
import com.example.domain.ArticleRepository
import com.example.domain.UserId
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ArticleQueryService(private val articleRepository: ArticleRepository) {
    fun findById(id: String): Article? = articleRepository.findById(ArticleId(UUID.fromString(id)))
    fun findAllByUserId(userId: String): List<Article> = articleRepository.findAllByUserId(UserId.of(userId))
    fun delete(id: String) = articleRepository.delete(ArticleId(UUID.fromString(id)))
}
