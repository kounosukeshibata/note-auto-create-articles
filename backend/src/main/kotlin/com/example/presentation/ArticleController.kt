package com.example.presentation

import com.example.application.AffiliateLinkDto
import com.example.application.ArticleQueryService
import com.example.application.GenerateAffiliateArticleUseCase
import com.example.application.GenerateArticleInput
import com.example.application.GenerateArticleOutput
import com.example.application.PostArticleToDraftUseCase
import com.example.domain.AffiliatePlatform
import com.example.domain.Article
import com.example.domain.ArticleNotFoundException
import com.example.domain.UserId
import com.example.presentation.dto.AffiliateLinkResponse
import com.example.presentation.dto.ArticleResponse
import com.example.presentation.dto.ArticleSummaryResponse
import com.example.presentation.dto.GenerateArticleRequest
import com.example.presentation.dto.GenerateArticleResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/articles")
class ArticleController(
    private val generateArticleUseCase: GenerateAffiliateArticleUseCase,
    private val postArticleToDraftUseCase: PostArticleToDraftUseCase,
    private val articleQueryService: ArticleQueryService,
) {
    @PostMapping("/generate")
    fun generate(
        @RequestBody @Validated request: GenerateArticleRequest,
        authentication: Authentication,
    ): ResponseEntity<GenerateArticleResponse> {
        val userId = UserId.of(authentication.principal as String)
        val platforms = request.affiliatePlatforms.map { AffiliatePlatform.valueOf(it.uppercase()) }
        val input = GenerateArticleInput(
            theme = request.theme,
            affiliatePlatforms = platforms,
            userId = userId,
            targetPainPoint = request.targetPainPoint ?: "",
            targetIdealState = request.targetIdealState ?: "",
            storyTrigger = request.storyTrigger ?: "",
            uniqueInsight = request.uniqueInsight ?: "",
            articleType = request.articleType ?: "一般",
            ctaInfo = request.ctaInfo ?: "",
        )
        val output = generateArticleUseCase.execute(input)
        return ResponseEntity.ok(output.toResponse())
    }

    @PostMapping("/{id}/draft")
    fun postDraft(
        @PathVariable id: String,
        authentication: Authentication,
    ): ResponseEntity<Map<String, String>> {
        val userId = UserId.of(authentication.principal as String)
        val output = postArticleToDraftUseCase.execute(id, userId)
        return ResponseEntity.ok(mapOf("noteUrl" to output.noteUrl))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ResponseEntity<ArticleResponse> {
        val article = articleQueryService.findById(id) ?: throw ArticleNotFoundException(id)
        return ResponseEntity.ok(article.toResponse())
    }

    @GetMapping
    fun findAll(authentication: Authentication): ResponseEntity<List<ArticleSummaryResponse>> {
        val articles = articleQueryService.findAllByUserId(authentication.principal as String)
        return ResponseEntity.ok(articles.map { it.toSummaryResponse() })
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Void> {
        articleQueryService.delete(id)
        return ResponseEntity.noContent().build()
    }

    private fun GenerateArticleOutput.toResponse() = GenerateArticleResponse(
        articleId = articleId,
        title = title,
        content = content,
        imageUrl = imageUrl,
        affiliateLinks = affiliateLinks.map { it.toResponse() },
        status = status,
    )

    private fun AffiliateLinkDto.toResponse() = AffiliateLinkResponse(
        url = url,
        trackingId = trackingId,
        platform = platform,
        productName = productName,
        price = price,
    )

    private fun Article.toResponse() = ArticleResponse(
        id = id.value.toString(),
        title = content.title,
        content = content.text,
        imageUrl = image.url,
        imageAltText = image.altText,
        keywords = keywords.map { it.value },
        affiliateLinks = productLinks.links.map { link ->
            AffiliateLinkResponse(
                url = link.url,
                trackingId = link.trackingId,
                platform = link.platform.name,
                productName = link.productInfo.name,
                price = link.productInfo.price,
            )
        },
        status = status.name,
    )

    private fun Article.toSummaryResponse() = ArticleSummaryResponse(
        id = id.value.toString(),
        title = content.title,
        status = status.name,
        imageUrl = image.url,
    )
}
