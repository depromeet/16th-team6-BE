package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.TMapTransitClient
import com.deepromeet.atcha.transit.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.TMapRouteResponse
import org.springframework.stereotype.Service

@Service
class TransitService(
    private val tMapTransitClient: TMapTransitClient,
    private val taxiFareFetcher: TaxiFareFetcher,
    private val busManager: BusManager
) {
    fun getRoutes(): TMapRouteResponse {
        return tMapTransitClient.getRoutes(
            TMapRouteRequest(
                startX = "126.978388",
                startY = "37.566610",
                endX = "127.027636",
                endY = "37.497950",
                count = 5,
                searchDttm = "202502142100"
            )
        )
    }

    fun getBusArrivalInfo(
        routeName: String,
        stationName: String
    ): BusArrival {
        return busManager.getArrivalInfo(routeName, stationName)
    }

    fun getTaxiFare(
        start: Coordinate,
        end: Coordinate
    ): Fare {
        return taxiFareFetcher.fetch(start, end)
            ?: throw TransitException.TaxiFareFetchFailed
    }
}
