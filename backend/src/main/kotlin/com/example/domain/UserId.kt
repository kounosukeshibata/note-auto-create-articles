package com.example.domain

import java.util.UUID

data class UserId(val value: UUID) {
    companion object {
        fun generate() = UserId(UUID.randomUUID())
        fun of(str: String) = UserId(UUID.fromString(str))
    }
}
