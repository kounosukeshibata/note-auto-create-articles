package com.example.presentation

import com.example.application.AffiliateApiUnavailableException
import com.example.domain.ArticleNotFoundException
import com.example.domain.DuplicateUserException
import com.example.domain.InvalidCredentialsException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    data class ErrorBody(val code: String, val message: String)
    data class ErrorResponse(val error: ErrorBody)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val msg = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "バリデーションエラー"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ErrorBody("VALIDATION_ERROR", msg)))
    }

    @ExceptionHandler(DuplicateUserException::class)
    fun handleDuplicateUser(ex: DuplicateUserException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ErrorBody("USER_ALREADY_EXISTS", "このメールアドレスはすでに登録されています")))

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(ErrorBody("INVALID_CREDENTIALS", "メールアドレスまたはパスワードが正しくありません")))

    @ExceptionHandler(ArticleNotFoundException::class)
    fun handleNotFound(ex: ArticleNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ErrorBody("NOT_FOUND", ex.message ?: "Not found")))

    @ExceptionHandler(AffiliateApiUnavailableException::class)
    fun handleAffiliateApiUnavailable(ex: AffiliateApiUnavailableException): ResponseEntity<ErrorResponse> {
        logger.error("Affiliate API unavailable: ${ex.message}")
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse(ErrorBody("AI_SERVICE_UNAVAILABLE", ex.message ?: "アフィリエイトAPIが利用できません")))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(ErrorBody("INTERNAL_SERVER_ERROR", "内部サーバーエラーが発生しました")))
    }
}
