package com.deepromeet.atcha.route.domain

data class RoutePassStops(
    val stops: List<RoutePassStop> = emptyList()
) {
    fun hasStops(): Boolean = stops.isNotEmpty()

    fun size(): Int = stops.size

    fun getStopAt(index: Int): RoutePassStop? = stops.getOrNull(index)

    fun findStopByName(name: String): RoutePassStop? = stops.find { it.stationName == name }

    fun firstStop(): RoutePassStop? = stops.firstOrNull()

    fun lastStop(): RoutePassStop? = stops.lastOrNull()

    companion object {
        fun empty(): RoutePassStops = RoutePassStops(emptyList())

        fun of(stops: List<RoutePassStop>): RoutePassStops = RoutePassStops(stops)

        fun of(stop: RoutePassStop): RoutePassStops = RoutePassStops(listOf(stop))
    }
}
