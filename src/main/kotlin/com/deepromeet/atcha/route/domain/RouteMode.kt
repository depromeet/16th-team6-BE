package com.deepromeet.atcha.route.domain

enum class RouteMode(val value: String) {
    WALK("WALK"),
    SUBWAY("SUBWAY"),
    BUS("BUS"),
    TAXI("TAXI"),
    CAR("CAR");

    fun isTransit(): Boolean = this == SUBWAY || this == BUS

    fun isWalk(): Boolean = this == WALK

    companion object {
        fun from(value: String): RouteMode {
            return entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown route mode: $value")
        }
    }
}
