package com.example.domain

@JvmInline
value class SeoKeyword(val value: String) {
    init {
        require(value.isNotBlank()) { "キーワードは空にできません" }
    }
}
