package com.example.application

import com.example.domain.ArticleId
import com.example.domain.ArticleNotFoundException
import com.example.domain.ArticleRepository
import com.example.domain.UserId
import com.example.infrastructure.NoteClient
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PostArticleToDraftUseCaseImpl(
    private val articleRepository: ArticleRepository,
    private val noteClient: NoteClient,
) : PostArticleToDraftUseCase {

    override fun execute(articleId: String, userId: UserId): PostToDraftOutput {
        val id = ArticleId(UUID.fromString(articleId))
        val article = articleRepository.findById(id) ?: throw ArticleNotFoundException(articleId)
        val result = noteClient.postDraft(article)
        if (!result.success) error("note への投稿に失敗しました")
        articleRepository.save(article.markAsDrafted())
        return PostToDraftOutput(noteUrl = result.noteUrl ?: "")
    }
}
