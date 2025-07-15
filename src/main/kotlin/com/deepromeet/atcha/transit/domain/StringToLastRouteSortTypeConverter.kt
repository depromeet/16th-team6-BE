package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.domain.route.LastRouteSortType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToLastRouteSortTypeConverter : Converter<String, LastRouteSortType> {
    override fun convert(source: String): LastRouteSortType? {
        return try {
            LastRouteSortType.fromCode(source.toInt())
                ?: throw IllegalArgumentException("Invalid sort type code: $source")
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid sort type code: $source", e)
        }
    }
}
