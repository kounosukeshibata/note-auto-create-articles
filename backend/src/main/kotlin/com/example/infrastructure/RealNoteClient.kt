package com.example.infrastructure

import com.example.domain.Article
import com.fasterxml.jackson.databind.ObjectMapper
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URI

@Component
@ConditionalOnProperty(name = ["note.stub"], havingValue = "false")
class RealNoteClient(
    @Value("\${note.session-cookie}") private val sessionCookie: String,
    @Value("\${note.id:}") private val noteId: String,
) : NoteClient {

    private val logger = LoggerFactory.getLogger(RealNoteClient::class.java)
    private val mapper = ObjectMapper()
    private val mdParser = Parser.builder().build()
    private val htmlRenderer = HtmlRenderer.builder().build()

    private fun markdownToHtml(md: String): String = htmlRenderer.render(mdParser.parse(md)).trim()

    override fun postDraft(article: Article): NotePostResult {
        if (sessionCookie.isBlank()) {
            logger.error("NOTE_SESSION_COOKIE が設定されていません")
            return NotePostResult(success = false)
        }
        return try {
            val (noteKey, noteId2) = createEmptyNote()
            if (article.image.url.isNotBlank()) {
                uploadEyecatch(noteId2, article.image.url)
            }
            saveDraftContent(noteId2, article)
            val noteUrl = when {
                noteId.isNotBlank() -> "https://note.com/$noteId/n/$noteKey"
                else -> "https://note.com"
            }
            logger.info("note.com 下書き作成成功: $noteUrl")
            NotePostResult(success = true, noteUrl = noteUrl)
        } catch (e: Exception) {
            logger.error("note.com への投稿に失敗しました: ${e.message}", e)
            NotePostResult(success = false)
        }
    }

    private fun createEmptyNote(): Pair<String, Long> {
        val body = mapper.writeValueAsString(mapOf("template_key" to null))
        val conn = openConnection("https://note.com/api/v1/text_notes", "POST")
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val status = conn.responseCode
        if (status !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "no body"
            throw RuntimeException("note作成失敗 ($status): $err")
        }

        val tree = mapper.readTree(conn.inputStream.bufferedReader().readText())
        val key = tree.path("data").path("key").asText()
        val id  = tree.path("data").path("id").asLong()
        return key to id
    }

    private fun saveDraftContent(noteId: Long, article: Article) {
        val htmlBody = markdownToHtml(article.content.text)
        val bodyLength = article.content.text.replace(Regex("#+ |\\*\\*?|__?|`|\\[|\\]\\([^)]*\\)"), "")
            .replace(Regex("\\s+"), " ").trim().length

        val payload = mapper.writeValueAsString(mapOf(
            "body"        to htmlBody,
            "body_length" to bodyLength,
            "name"        to article.content.title,
            "index"       to false,
            "is_lead_form" to false,
        ))
        val conn = openConnection(
            "https://note.com/api/v1/text_notes/draft_save?id=$noteId&is_temp_saved=true",
            "POST"
        )
        conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

        val status = conn.responseCode
        if (status !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "no body"
            throw RuntimeException("下書き保存失敗 ($status): $err")
        }
    }

    private fun uploadEyecatch(noteId: Long, imageUrl: String) {
        val imageBytes = try {
            URI.create(imageUrl).toURL().openStream().use { it.readBytes() }
        } catch (e: Exception) {
            logger.warn("アイキャッチ画像のダウンロードに失敗しました: $imageUrl — ${e.message}")
            return
        }

        val boundary = "----FormBoundary${System.currentTimeMillis()}"
        val conn = URI.create("https://note.com/api/v1/image_upload/note_eyecatch").toURL()
            .openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
        conn.setRequestProperty("x-requested-with", "XMLHttpRequest")
        conn.setRequestProperty("Origin", "https://editor.note.com")
        conn.setRequestProperty("Referer", "https://editor.note.com/")
        conn.setRequestProperty("Cookie", sessionCookie)

        conn.outputStream.use { out ->
            fun field(name: String, value: String) {
                out.write("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n".toByteArray())
            }
            field("note_id", noteId.toString())
            field("width", "800")
            field("height", "400")
            out.write("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"eyecatch.png\"\r\nContent-Type: image/png\r\n\r\n".toByteArray())
            out.write(imageBytes)
            out.write("\r\n--$boundary--\r\n".toByteArray())
        }

        val status = conn.responseCode
        if (status !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "no body"
            logger.warn("アイキャッチ画像アップロード失敗 ($status): $err")
        } else {
            logger.info("アイキャッチ画像アップロード成功")
        }
    }

    private fun openConnection(urlStr: String, method: String): HttpURLConnection {
        val conn = URI.create(urlStr).toURL().openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 30_000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
        conn.setRequestProperty("x-requested-with", "XMLHttpRequest")
        conn.setRequestProperty("Origin", "https://editor.note.com")
        conn.setRequestProperty("Referer", "https://editor.note.com/")
        conn.setRequestProperty("Cookie", sessionCookie)
        return conn
    }
}
