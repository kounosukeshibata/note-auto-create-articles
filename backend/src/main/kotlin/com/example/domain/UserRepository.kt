package com.example.domain

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun findById(id: UserId): User?
}
