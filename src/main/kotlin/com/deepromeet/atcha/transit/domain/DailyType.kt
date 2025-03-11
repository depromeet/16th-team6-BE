package com.deepromeet.atcha.transit.domain

enum class DailyType(
    val code: String
) {
    WEEKDAY("01"),
    SATURDAY("02"),
    HOLIDAY("03");

    companion object {
        fun fromCode(code: String): DailyType {
            return entries.first { it.code == code }
        }
    }
}
