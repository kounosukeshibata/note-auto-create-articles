package com.example.presentation.dto

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val name: String,
)
