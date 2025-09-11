package com.deepromeet.atcha.route.infrastructure.client.tmap

import com.deepromeet.atcha.route.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.route.infrastructure.client.tmap.response.TMapRouteResponse
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange
interface TMapRouteHttpClient {
    @PostExchange("/transit/routes")
    fun getRoutes(
        @RequestBody request: TMapRouteRequest
    ): TMapRouteResponse
}
