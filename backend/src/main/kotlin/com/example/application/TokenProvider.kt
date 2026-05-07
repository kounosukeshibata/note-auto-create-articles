package com.example.application

interface TokenProvider {
    fun generateToken(userId: String): String
    fun validateToken(token: String): String?
}
