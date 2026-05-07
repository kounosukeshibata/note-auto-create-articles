package com.example.domain

class LinkReplacementService {
    fun replace(content: Content, productLinks: ProductLinks): Content {
        var text = content.text
        productLinks.links.forEachIndexed { index, link ->
            text = text.replace("{{product_link_$index}}", link.url)
        }
        return content.copy(text = text)
    }
}
