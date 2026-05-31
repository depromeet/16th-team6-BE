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

        if (!hasOnlyValidModes(transitModes)) return false

        // 순수 버스 3개 경로는 중간 버스/버스 수 제한을 예외 허용한다.
        // (LastRouteCalculator가 가장 이른 막차 버스에 배차간격 버퍼를 둬 환승 위험을 보완한다.)
        if (isBusOnlyThreeLegs(transitModes)) return true

        return hasNoBusInMiddle(transitModes) && hasValidBusCount(transitModes)
    }

    private fun isBusOnlyThreeLegs(transitModes: List<RouteMode>): Boolean =
        transitModes.size == 3 && transitModes.all { it == RouteMode.BUS }

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
