package com.example.application

import com.example.domain.AffiliatePlatform
import com.example.domain.Article
import com.example.domain.ArticleId
import com.example.domain.ArticleStatus
import com.example.domain.Content
import com.example.domain.Image
import com.example.domain.LinkReplacementService
import com.example.domain.ProductInfo
import com.example.domain.ProductLinks
import com.example.domain.SeoKeyword
import com.example.domain.UserId
import com.example.infrastructure.AffiliateApiClient
import com.example.infrastructure.VertexAiClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.UUID

class GenerateAffiliateArticleUseCaseTest {

    private lateinit var vertexAiClient: VertexAiClient
    private lateinit var amazonClient: AffiliateApiClient
    private lateinit var articleRepository: com.example.domain.ArticleRepository
    private lateinit var useCase: GenerateAffiliateArticleUseCaseImpl

    private val sampleUserId = UserId(UUID.randomUUID())
    private val sampleKeywords = listOf(SeoKeyword("アウトドア"), SeoKeyword("おすすめ"))
    private val sampleContent = Content(title = "アウトドアの完全ガイド", text = "記事本文 {{product_link_0}}")
    private val sampleImage = Image(url = "https://example.com/image.png", altText = "アウトドア")
    private val sampleProduct = ProductInfo(
        name = "テスト商品",
        price = BigDecimal("10000"),
        category = "outdoor",
        thumbnailUrl = "https://example.com/thumb.png",
        commissionRate = 0.08,
        affiliateUrl = "https://www.amazon.co.jp/dp/B0EXAMPLE?tag=test-22",
    )

    @BeforeEach
    fun setUp() {
        vertexAiClient = mock()
        amazonClient = mock()
        articleRepository = mock()

        whenever(amazonClient.platform).thenReturn(AffiliatePlatform.AMAZON)

        useCase = GenerateAffiliateArticleUseCaseImpl(
            vertexAiClient = vertexAiClient,
            affiliateApiClients = listOf(amazonClient),
            articleRepository = articleRepository,
            linkReplacementService = LinkReplacementService(),
        )
    }

    private fun defaultInput() = GenerateArticleInput(
        theme = "アウトドア",
        affiliatePlatforms = listOf(AffiliatePlatform.AMAZON),
        userId = sampleUserId,
        targetPainPoint = "体力に自信がない",
        targetIdealState = "楽しく登山できる",
        articleType = "アフィリエイト",
    )

    private fun stubSavedArticle(id: ArticleId = ArticleId(UUID.randomUUID())): Article = Article(
        id = id,
        userId = sampleUserId,
        content = sampleContent,
        image = sampleImage,
        keywords = sampleKeywords,
        productLinks = ProductLinks(),
        status = ArticleStatus.SAVED,
    )

    @Test
    fun `正常系 - 記事生成成功し SAVED ステータスで返却される`() {
        val savedArticle = stubSavedArticle()

        whenever(vertexAiClient.extractKeywords(any())).thenReturn(sampleKeywords)
        whenever(amazonClient.searchProducts(any())).thenReturn(listOf(sampleProduct))
        whenever(vertexAiClient.generateContent(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyOrNull())).thenReturn(sampleContent)
        whenever(vertexAiClient.generateImage(any())).thenReturn(sampleImage)
        whenever(articleRepository.save(any())).thenReturn(savedArticle)

        val output = useCase.execute(defaultInput())

        assertEquals(ArticleStatus.SAVED.name, output.status)
        assertNotNull(output.articleId)
    }

    @Test
    fun `正常系 - アフィリエイトリンクが商品の affiliateUrl から設定される`() {
        whenever(vertexAiClient.extractKeywords(any())).thenReturn(sampleKeywords)
        whenever(amazonClient.searchProducts(any())).thenReturn(listOf(sampleProduct))
        whenever(vertexAiClient.generateContent(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyOrNull())).thenReturn(sampleContent)
        whenever(vertexAiClient.generateImage(any())).thenReturn(sampleImage)
        // save を呼び出した引数をそのまま返す（リンク注入済みの記事を保持）
        whenever(articleRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as Article }

        val output = useCase.execute(defaultInput())

        assertEquals(1, output.affiliateLinks.size)
        assertEquals(sampleProduct.affiliateUrl, output.affiliateLinks.first().url)
    }

    @Test
    fun `affiliate API 障害時 - AffiliateApiUnavailableException がスローされる`() {
        whenever(vertexAiClient.extractKeywords(any())).thenReturn(sampleKeywords)
        whenever(amazonClient.searchProducts(any())).thenThrow(RuntimeException("Amazon API is down"))

        assertThrows(AffiliateApiUnavailableException::class.java) {
            useCase.execute(defaultInput())
        }
    }
}
