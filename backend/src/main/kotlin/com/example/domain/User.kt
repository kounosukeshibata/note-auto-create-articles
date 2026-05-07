package com.example.domain

data class User(
    val id: UserId,
    val email: String,
    val passwordHash: String,
    val name: String,
)
