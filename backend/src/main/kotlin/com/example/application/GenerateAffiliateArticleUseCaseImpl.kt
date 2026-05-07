package com.example.application

import com.example.domain.AffiliateLink
import com.example.domain.AffiliatePlatform
import com.example.domain.Article
import com.example.domain.ArticleId
import com.example.domain.ArticleRepository
import com.example.domain.LinkReplacementService
import com.example.domain.ProductInfo
import com.example.domain.ProductLinks
import com.example.domain.SeoKeyword
import com.example.infrastructure.AffiliateApiClient
import com.example.infrastructure.VertexAiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GenerateAffiliateArticleUseCaseImpl(
    private val vertexAiClient: VertexAiClient,
    private val affiliateApiClients: List<AffiliateApiClient>,
    private val articleRepository: ArticleRepository,
    private val linkReplacementService: LinkReplacementService,
) : GenerateAffiliateArticleUseCase {

    private val logger = LoggerFactory.getLogger(GenerateAffiliateArticleUseCaseImpl::class.java)

    /** プラットフォームと商品情報のペア */
    private data class PlatformProduct(val platform: AffiliatePlatform, val productInfo: ProductInfo)

    override fun execute(input: GenerateArticleInput): GenerateArticleOutput {
        // 1. キーワード抽出
        val keywords: List<SeoKeyword> = vertexAiClient.extractKeywords(input.theme)

        // 2. 対象プラットフォームのクライアントを絞り込み、商品検索（プラットフォーム情報を保持）
        val targetClients = affiliateApiClients.filter { it.platform in input.affiliatePlatforms }
        val allProducts = searchProductsWithFallback(targetClients, keywords)

        // 還元率の高い順にソートして上位商品を使用
        val topProducts = allProducts.sortedByDescending { it.productInfo.commissionRate }.take(MAX_PRODUCTS)

        // 3. 記事生成と画像生成（スタブ実装では直列）
        val content = vertexAiClient.generateContent(
            theme = input.theme,
            keywords = keywords,
            products = topProducts.map { it.productInfo },
            targetPainPoint = input.targetPainPoint,
            targetIdealState = input.targetIdealState,
            storyTrigger = input.storyTrigger,
            uniqueInsight = input.uniqueInsight,
            articleType = input.articleType,
            ctaInfo = input.ctaInfo,
        )
        val image = vertexAiClient.generateImage(input.theme)

        // 4. ProductLinks を構築（プラットフォーム情報を正確に保持）
        val productLinks = buildProductLinks(topProducts)

        // 5. Article 集約を生成してリンク注入
        val article = Article(
            id = ArticleId.generate(),
            userId = input.userId,
            content = content,
            image = image,
            keywords = keywords,
            productLinks = productLinks,
        ).injectLinks(linkReplacementService)

        // 6. 保存して返す（note投稿はユーザー操作で別途実行）
        val saved = articleRepository.save(article.markAsSaved())

        return GenerateArticleOutput(
            articleId = saved.id.value.toString(),
            title = saved.content.title,
            content = saved.content.text,
            imageUrl = saved.image.url,
            affiliateLinks = saved.productLinks.links.map { link ->
                AffiliateLinkDto(
                    url = link.url,
                    trackingId = link.trackingId,
                    platform = link.platform.name,
                    productName = link.productInfo.name,
                    price = link.productInfo.price,
                )
            },
            status = saved.status.name,
        )
    }

    private fun searchProductsWithFallback(
        clients: List<AffiliateApiClient>,
        keywords: List<SeoKeyword>,
    ): List<PlatformProduct> {
        val results = mutableListOf<PlatformProduct>()
        val errors = mutableListOf<Throwable>()

        for (client in clients) {
            try {
                val products = client.searchProducts(keywords).map { PlatformProduct(client.platform, it) }
                results += products
            } catch (e: Exception) {
                logger.warn("Affiliate API failed for platform ${client.platform}: ${e.message}")
                errors += e
            }
        }

        if (results.isEmpty() && clients.isNotEmpty()) {
            throw AffiliateApiUnavailableException(
                "All affiliate APIs are unavailable: ${errors.joinToString { it.message ?: "unknown" }}"
            )
        }

        return results
    }

    private fun buildProductLinks(products: List<PlatformProduct>): ProductLinks {
        var productLinks = ProductLinks()
        products.forEachIndexed { index, (platform, productInfo) ->
            val link = AffiliateLink(
                url = productInfo.affiliateUrl.ifBlank {
                    "https://www.amazon.co.jp/s?k=${productInfo.name}&tag=noteautopost-22"
                },
                trackingId = "tracking-${platform.name.lowercase()}-$index",
                platform = platform,
                productInfo = productInfo,
            )
            productLinks = productLinks.add(link)
        }
        return productLinks
    }

    companion object {
        private const val MAX_PRODUCTS = 5
    }
}
