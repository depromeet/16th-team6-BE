package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.infrastructure.client.response.TMapPOIResponse
import com.deepromeet.atcha.location.infrastructure.client.response.TMapReverseGeocodingResponse
import com.deepromeet.atcha.location.infrastructure.client.response.TMapReverseLabelResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface TMapLocationHttpClient {
    @GetExchange("/tmap/pois")
    suspend fun getPOIs(
        @RequestParam searchKeyword: String,
        @RequestParam centerLat: Double,
        @RequestParam centerLon: Double,
        @RequestParam page: Int = 1,
        @RequestParam count: Int = 20
    ): TMapPOIResponse?

    @GetExchange("/tmap/geo/reverseLabel")
    suspend fun getReverseLabel(
        @RequestParam centerLat: Double,
        @RequestParam centerLon: Double,
        @RequestParam reqCoordType: String = "WGS84GEO",
        @RequestParam resCoordType: String = "WGS84GEO",
        @RequestParam reqLevel: Int = 19
    ): TMapReverseLabelResponse

    @GetExchange("/tmap/geo/reversegeocoding")
    suspend fun getReverseGeocoding(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam addressType: String = "A03",
        @RequestParam coordType: String = "WGS84GEO"
    ): TMapReverseGeocodingResponse
}
