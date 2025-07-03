package com.deepromeet.atcha.transit.domain

enum class DailyType(
    val code: String,
    val description: String
) {
    WEEKDAY("01", "평일"),
    SATURDAY("02", "주말"),
    SUNDAY("NONE", "주말"),
    HOLIDAY("03", "공휴일")
}
