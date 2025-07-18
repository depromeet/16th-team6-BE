package com.deepromeet.atcha.route.domain

enum class RouteMode(
    val value: String,
    val isSupported: Boolean = true,
    val requiresDepartureTime: Boolean = false
) {
    WALK("WALK", isSupported = true, requiresDepartureTime = false),
    SUBWAY("SUBWAY", isSupported = true, requiresDepartureTime = true),
    BUS("BUS", isSupported = true, requiresDepartureTime = true),
    EXPRESS_BUS("EXPRESSBUS", isSupported = false),
    TRAIN("TRAIN", isSupported = false),
    AIRPLANE("AIRPLANE", isSupported = false),
    FERRY("FERRY", isSupported = false);

    fun isTransit(): Boolean = this == SUBWAY || this == BUS

    fun isWalk(): Boolean = this == WALK

    companion object {
        fun from(value: String): RouteMode {
            return entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown route mode: $value")
        }
    }
}
