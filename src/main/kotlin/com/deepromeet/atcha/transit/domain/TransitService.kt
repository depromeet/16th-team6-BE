package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.infrastructure.client.TMapTransitClient
import com.deepromeet.atcha.transit.infrastructure.client.request.TMapRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.response.TMapRouteResponse
import org.springframework.stereotype.Service

@Service
class TransitService(
    private val tMapTransitClient: TMapTransitClient
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
}
