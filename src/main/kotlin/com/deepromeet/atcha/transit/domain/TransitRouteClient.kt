package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.common.feign.FeignException
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.TMapTransitClient
import com.deepromeet.atcha.transit.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

@Component
class TransitRouteClient(
    private val tMapTransitClient: TMapTransitClient,
    private val regionIdentifier: RegionIdentifier
) {
    fun fetchItineraries(
        start: Coordinate,
        end: Coordinate
    ): List<Itinerary> {
        validateServiceRegion(start, end)

        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val baseDate = today.format(dateFormatter) + "2300"

        val response = try {
            tMapTransitClient.getRoutes(
                TMapRouteRequest(
                    startX = start.lon.toString(),
                    startY = start.lat.toString(),
                    endX = end.lon.toString(),
                    endY = end.lat.toString(),
                    count = 20,
                    searchDttm = baseDate
                )
            )
        } catch (e: FeignException) {
            log.error(e) { "TMap 경로 검색 API 호출 중 에러 발생" }
            throw TransitException.of(TransitError.TMAP_TIME_OUT, e)
        }

        response.result?.let { result ->
            when (result.status) {
                11 -> throw TransitException.of(TransitError.DISTANCE_TOO_SHORT)
                else -> throw TransitException.of(TransitError.SERVICE_AREA_NOT_SUPPORTED)
            }
        }

        return filterValidItineraries(
            response.metaData?.plan?.itineraries
                ?: throw TransitException.of(
                    TransitError.TRANSIT_API_ERROR,
                    "경로 검색 API에서 유효한 여행 경로를 반환하지 않았습니다."
                )
        )
    }

    private fun filterValidItineraries(itineraries: List<Itinerary>): List<Itinerary> {
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

    private fun validateServiceRegion(
        start: Coordinate,
        destination: Coordinate
    ) {
        regionIdentifier.identify(start)
        regionIdentifier.identify(destination)
    }
}
