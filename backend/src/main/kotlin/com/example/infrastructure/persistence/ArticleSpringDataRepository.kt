package com.example.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ArticleSpringDataRepository : JpaRepository<ArticleJpaEntity, UUID> {
    fun findByUserId(userId: UUID): List<ArticleJpaEntity>
}
