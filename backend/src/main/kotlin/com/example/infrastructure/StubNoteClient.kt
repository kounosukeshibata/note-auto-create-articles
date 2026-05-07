package com.example.infrastructure

import com.example.domain.Article
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["note.stub"], havingValue = "true", matchIfMissing = true)
class StubNoteClient(
    @Value("\${note.id:}") private val noteId: String,
) : NoteClient {
    override fun postDraft(article: Article): NotePostResult {
        val url = if (noteId.isNotBlank()) "https://note.com/$noteId" else "https://note.com/draft/stub"
        return NotePostResult(success = true, noteUrl = url)
    }
}
