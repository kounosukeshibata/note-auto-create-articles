package com.example.domain

data class Article(
    val id: ArticleId,
    val userId: UserId,
    val content: Content,
    val image: Image,
    val keywords: List<SeoKeyword>,
    val productLinks: ProductLinks,
    val status: ArticleStatus = ArticleStatus.GENERATED,
) {
    fun injectLinks(service: LinkReplacementService): Article =
        copy(content = service.replace(content, productLinks))

    fun markAsSaved(): Article = copy(status = ArticleStatus.SAVED)

    fun markAsDrafted(): Article = copy(status = ArticleStatus.NOTE_DRAFTED)
}
