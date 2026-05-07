package com.example.infrastructure.persistence

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

private val mapper = ObjectMapper().findAndRegisterModules()

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attr: List<String>?): String =
        mapper.writeValueAsString(attr ?: emptyList<String>())

    override fun convertToEntityAttribute(data: String?): List<String> =
        if (data.isNullOrBlank()) emptyList()
        else mapper.readValue(data, object : TypeReference<List<String>>() {})
}

@Converter
class AffiliateLinkListConverter : AttributeConverter<List<AffiliateLinkJson>, String> {
    override fun convertToDatabaseColumn(attr: List<AffiliateLinkJson>?): String =
        mapper.writeValueAsString(attr ?: emptyList<AffiliateLinkJson>())

    override fun convertToEntityAttribute(data: String?): List<AffiliateLinkJson> =
        if (data.isNullOrBlank()) emptyList()
        else mapper.readValue(data, object : TypeReference<List<AffiliateLinkJson>>() {})
}
