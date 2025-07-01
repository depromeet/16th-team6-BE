package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

object ItineraryValidator {
    fun filterValidItineraries(itineraries: List<Itinerary>): List<Itinerary> {
        fun isValidItinerary(itinerary: Itinerary): Boolean {
            var hasValidModes = false
            var hasExpressSubway = false
            var busCount = 0
            var transitCount = 0
            var isFirstTransit = true
            var hasInvalid = false
            var hasNBUS = false

            for (leg in itinerary.legs) {
                when (leg.mode) {
                    "WALK" -> hasValidModes = true
                    "SUBWAY" -> {
                        transitCount++
                        if (leg.route?.contains("(급행)") == true) {
                            hasExpressSubway = true
                        } else {
                            hasValidModes = true
                        }
                    }
                    "BUS" -> {
                        transitCount++
                        if (!isFirstTransit) {
                            busCount++
                        }
                        hasValidModes = true
                        if (leg.route!!.contains("N")) {
                            hasNBUS = true
                        }
                        isFirstTransit = false
                    }
                    else -> {
                        hasInvalid = true
                        break
                    }
                }
            }

            return !hasInvalid &&
                !hasExpressSubway &&
                hasValidModes &&
                !hasNBUS &&
                busCount <= 2 &&
                transitCount < 4
        }

        val filtered = itineraries.filter { itinerary -> isValidItinerary(itinerary) }
        log.info { "필터링 로직에서 ${itineraries.size}개의 경로 중 ${filtered.size}개가 유효한 경로로 필터링되었습니다." }
        return filtered
    }
}
