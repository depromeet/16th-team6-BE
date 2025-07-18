package com.deepromeet.atcha.route.domain

data class RoutePassStops(
    val stops: List<RoutePassStop> = emptyList()
) {
    companion object {
        fun of(stops: List<RoutePassStop>): RoutePassStops = RoutePassStops(stops)
    }
}
