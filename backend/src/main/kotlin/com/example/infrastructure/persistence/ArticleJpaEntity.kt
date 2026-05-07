package com.example.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "articles")
class ArticleJpaEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 500)
    var title: String = "",

    @Column(nullable = false, columnDefinition = "text")
    var content: String = "",

    @Column(name = "image_url", nullable = false, columnDefinition = "text")
    var imageUrl: String = "",

    @Column(name = "image_alt_text", nullable = false, length = 500)
    var imageAltText: String = "",

    @Convert(converter = StringListConverter::class)
    @Column(nullable = false, columnDefinition = "text")
    var keywords: List<String> = emptyList(),

    @Convert(converter = AffiliateLinkListConverter::class)
    @Column(name = "affiliate_links", nullable = false, columnDefinition = "text")
    var affiliateLinks: List<AffiliateLinkJson> = emptyList(),

    @Column(nullable = false, length = 50)
    var status: String = "",
)
