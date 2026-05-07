package com.example.domain

data class ProductLinks(val links: List<AffiliateLink> = emptyList()) {
    fun add(link: AffiliateLink): ProductLinks = copy(links = links + link)
}
