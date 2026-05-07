package com.example.application

interface GenerateAffiliateArticleUseCase {
    fun execute(input: GenerateArticleInput): GenerateArticleOutput
}
