package com.example.infrastructure

import com.example.application.TokenProvider
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long,
) : TokenProvider {

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    override fun generateToken(userId: String): String =
        Jwts.builder()
            .subject(userId)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact()

    override fun validateToken(token: String): String? =
        try {
            Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).payload.subject
        } catch (_: JwtException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
}
