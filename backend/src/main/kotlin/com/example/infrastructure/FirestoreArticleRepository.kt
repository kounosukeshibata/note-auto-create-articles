package com.example.infrastructure

import com.example.domain.Article
import com.example.domain.ArticleId
import com.example.domain.ArticleRepository
import com.example.domain.UserId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
@ConditionalOnProperty(name = ["storage.type"], havingValue = "memory", matchIfMissing = true)
class InMemoryArticleRepository : ArticleRepository {

    private val store = ConcurrentHashMap<ArticleId, Article>()

    override fun save(article: Article): Article {
        store[article.id] = article
        return article
    }

    override fun findById(id: ArticleId): Article? = store[id]

    override fun findAllByUserId(userId: UserId): List<Article> = store.values.filter { it.userId == userId }

    override fun delete(id: ArticleId) {
        store.remove(id)
    }
}
