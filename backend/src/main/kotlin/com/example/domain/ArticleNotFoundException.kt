package com.example.domain

class ArticleNotFoundException(id: String) : RuntimeException("Article not found: $id")
