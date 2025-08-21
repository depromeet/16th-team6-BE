package com.deepromeet.atcha.route.domain

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

object ItineraryValidator {
    fun filterValidItineraries(itineraries: List<RouteItinerary>): List<RouteItinerary> {
        val filtered = itineraries.filter { itinerary -> isValidItinerary(itinerary) }
        log.info { "필터링 로직에서 ${itineraries.size}개의 경로 중 ${filtered.size}개가 유효한 경로로 필터링되었습니다." }
        return filtered
    }

    private fun isValidItinerary(itinerary: RouteItinerary): Boolean {
        if (itinerary.transferCount > 4) return false

        val transitModes = extractTransitModes(itinerary.legs)

        return hasOnlyValidModes(transitModes) &&
            hasNoBusInMiddle(transitModes) &&
            hasValidBusCount(transitModes)
    }

    private fun extractTransitModes(legs: List<RouteLeg>): List<RouteMode> {
        return legs.filter { it.mode != RouteMode.WALK }.map { it.mode }
    }

    private fun hasOnlyValidModes(transitModes: List<RouteMode>): Boolean {
        return transitModes.all { it in setOf(RouteMode.SUBWAY, RouteMode.BUS) }
    }

    private fun hasNoBusInMiddle(transitModes: List<RouteMode>): Boolean {
        return transitModes.withIndex().none { (index, mode) ->
            mode == RouteMode.BUS &&
                index > 0 &&
                index < transitModes.size - 1
        }
    }

    private fun hasValidBusCount(transitModes: List<RouteMode>): Boolean {
        val busCount = transitModes.count { it == RouteMode.BUS }
        return busCount <= 2
    }
}
