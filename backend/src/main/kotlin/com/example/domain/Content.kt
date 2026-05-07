package com.example.domain

data class Content(
    val title: String,
    val text: String,
) {
    init {
        require(title.isNotBlank()) { "タイトルは必須です" }
    }
}
