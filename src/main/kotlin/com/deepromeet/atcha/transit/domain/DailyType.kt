package com.deepromeet.atcha.transit.domain

enum class DailyType(
    val code: String
) {
    WEEKDAY("01"),
    SATURDAY("02"),
    SUNDAY("NONE"),
    HOLIDAY("03")
}
