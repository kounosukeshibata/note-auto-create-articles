package com.example.infrastructure.persistence

import com.example.domain.AffiliateLink
import com.example.domain.AffiliatePlatform
import com.example.domain.Article
import com.example.domain.ArticleId
import com.example.domain.ArticleRepository
import com.example.domain.ArticleStatus
import com.example.domain.Content
import com.example.domain.Image
import com.example.domain.ProductInfo
import com.example.domain.ProductLinks
import com.example.domain.SeoKeyword
import com.example.domain.UserId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
@ConditionalOnProperty(name = ["storage.type"], havingValue = "supabase")
class JpaArticleRepository(
    private val springRepo: ArticleSpringDataRepository,
) : ArticleRepository {

    override fun save(article: Article): Article {
        springRepo.save(article.toEntity())
        return article
    }

    override fun findById(id: ArticleId): Article? =
        springRepo.findById(id.value).orElse(null)?.toDomain()

    override fun findAllByUserId(userId: UserId): List<Article> =
        springRepo.findByUserId(userId.value).map { it.toDomain() }

    override fun delete(id: ArticleId) {
        springRepo.deleteById(id.value)
    }

    private fun Article.toEntity() = ArticleJpaEntity(
        id = id.value,
        userId = userId.value,
        title = content.title,
        content = content.text,
        imageUrl = image.url,
        imageAltText = image.altText,
        keywords = keywords.map { it.value },
        affiliateLinks = productLinks.links.map { link ->
            AffiliateLinkJson(
                url = link.url,
                trackingId = link.trackingId,
                platform = link.platform.name,
                productName = link.productInfo.name,
                price = link.productInfo.price.toPlainString(),
                category = link.productInfo.category,
                thumbnailUrl = link.productInfo.thumbnailUrl,
                commissionRate = link.productInfo.commissionRate,
            )
        },
        status = status.name,
    )

    private fun ArticleJpaEntity.toDomain() = Article(
        id = ArticleId(id),
        userId = UserId(userId),
        content = Content(title = title, text = content),
        image = Image(url = imageUrl, altText = imageAltText),
        keywords = keywords.map { SeoKeyword(it) },
        productLinks = ProductLinks(affiliateLinks.map { json ->
            AffiliateLink(
                url = json.url,
                trackingId = json.trackingId,
                platform = AffiliatePlatform.valueOf(json.platform),
                productInfo = ProductInfo(
                    name = json.productName,
                    price = BigDecimal(json.price),
                    category = json.category,
                    thumbnailUrl = json.thumbnailUrl,
                    commissionRate = json.commissionRate,
                ),
            )
        }),
        status = ArticleStatus.valueOf(status),
    )
}
