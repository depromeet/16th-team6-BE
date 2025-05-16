package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.odsay.ODSayTransitClient
import com.deepromeet.atcha.transit.infrastructure.client.odsay.request.ODSayRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.odsay.response.ODSayItinerary
import com.deepromeet.atcha.transit.infrastructure.client.tmap.TMapTransitClient
import com.deepromeet.atcha.transit.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class TransitRouteClient(
    private val tMapTransitClient: TMapTransitClient,
    private val oDSayTransitClient: ODSayTransitClient,
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

        val response =
            tMapTransitClient.getRoutes(
                TMapRouteRequest(
                    startX = start.lon.toString(),
                    startY = start.lat.toString(),
                    endX = end.lon.toString(),
                    endY = end.lat.toString(),
                    count = 10,
                    searchDttm = baseDate
                )
            )

        response.result?.let { result ->
            when (result.status) {
                11 -> throw TransitException.DistanceTooShort
                else -> throw TransitException.ServiceAreaNotSupported
            }
        }

        return filterValidItineraries(response.metaData?.plan?.itineraries ?: throw TransitException.TransitApiError)
    }

    fun fetchItineraries_v2(
        start: Coordinate,
        end: Coordinate
    ): List<ODSayItinerary> {
        validateServiceRegion(start, end)

        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val baseDate = today.format(dateFormatter) + "2300"

        val response =
            oDSayTransitClient.getRoutes(
                ODSayRouteRequest(
                    startX = start.lon.toString(),
                    startY = start.lat.toString(),
                    endX = end.lon.toString(),
                    endY = end.lat.toString(),
                    count = 10,
                    searchDttm = baseDate
                )
            )

        response.result?.let { result ->
            when (result.status) {
                11 -> throw TransitException.DistanceTooShort
                else -> throw TransitException.ServiceAreaNotSupported
            }
        }

        return filterValidItineraries_v2(response.metaData?.plan?.itineraries ?: throw TransitException.TransitApiError)
    }

    private fun filterValidItineraries(itineraries: List<Itinerary>): List<Itinerary> {
        fun isValidItinerary(itinerary: Itinerary): Boolean {
            var hasValidModes = false
            var hasExpressSubway = false
            var busCount = 0
            var transitCount = 0
            var isFirstTransit = true
            var hasInvalid = false

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
                busCount <= 2 &&
                transitCount < 4
        }

        return itineraries.filter { itinerary -> isValidItinerary(itinerary) }
    }

    private fun filterValidItineraries_v2(itineraries: List<ODSayItinerary>): List<ODSayItinerary> {
        fun isValidItinerary(odSayItinerary: ODSayItinerary): Boolean {
            var hasValidModes = false
            var hasExpressSubway = false
            var busCount = 0
            var transitCount = 0
            var isFirstTransit = true
            var hasInvalid = false

            for (leg in odSayItinerary.legs) {
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
                busCount <= 2 &&
                transitCount < 4
        }

        return itineraries.filter { itinerary -> isValidItinerary(itinerary) }
    }

    private fun validateServiceRegion(
        start: Coordinate,
        destination: Coordinate
    ) {
        regionIdentifier.identify(start)
        regionIdentifier.identify(destination)
    }
}
