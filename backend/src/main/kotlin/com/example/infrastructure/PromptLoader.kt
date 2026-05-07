package com.example.infrastructure

import org.springframework.core.io.ClassPathResource

object PromptLoader {
    private val cache = HashMap<String, String>()

    fun load(path: String): String = cache.getOrPut(path) {
        val resource = ClassPathResource(path)
        check(resource.exists()) { "Prompt file not found on classpath: $path" }
        resource.inputStream.bufferedReader().readText()
    }
}
