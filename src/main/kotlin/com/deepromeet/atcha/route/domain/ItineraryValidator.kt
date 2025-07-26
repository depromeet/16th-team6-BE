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

        var busCount = 0
        var isFirstTransit = true
        var hasInvalidMode = false

        for (leg in itinerary.legs) {
            when (leg.mode) {
                RouteMode.WALK -> {
                    // 도보는 항상 유효
                }
                RouteMode.SUBWAY -> {
                    // 지하철은 항상 유효
                }
                RouteMode.BUS -> {
                    // 첫 번째 대중교통이 아닌 버스만 카운트
                    if (!isFirstTransit) {
                        busCount++
                    }
                    isFirstTransit = false
                }
                else -> {
                    hasInvalidMode = true
                    break
                }
            }
        }

        // 3. 최종 검증: 유효하지 않은 교통수단이 없고, 첫 버스 제외 버스가 1개 이하(총 2개)
        return !hasInvalidMode && busCount <= 1
    }
}
