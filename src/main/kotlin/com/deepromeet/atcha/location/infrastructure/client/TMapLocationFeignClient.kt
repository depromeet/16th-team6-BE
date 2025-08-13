package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.infrastructure.client.config.TMapFeignConfig
import com.deepromeet.atcha.location.infrastructure.client.response.TMapPOIResponse
import com.deepromeet.atcha.location.infrastructure.client.response.TMapReverseGeocodingResponse
import com.deepromeet.atcha.location.infrastructure.client.response.TMapReverseLabelResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "tmap-location",
    url = "\${tmap.api.url}",
    configuration = [TMapFeignConfig::class]
)
interface TMapLocationFeignClient {
    @GetMapping("/tmap/pois")
    fun getPOIs(
        @RequestParam searchKeyword: String,
        @RequestParam centerLat: Double,
        @RequestParam centerLon: Double,
        @RequestParam page: Int = 1,
        @RequestParam count: Int = 20
    ): TMapPOIResponse?

    @GetMapping("/tmap/geo/reverseLabel")
    fun getReverseLabel(
        @RequestParam centerLat: Double,
        @RequestParam centerLon: Double,
        @RequestParam reqCoordType: String = "WGS84GEO",
        @RequestParam resCoordType: String = "WGS84GEO",
        @RequestParam reqLevel: Int = 19
    ): TMapReverseLabelResponse

    @GetMapping("/tmap/geo/reversegeocoding")
    fun getReverseGeocoding(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam addressType: String = "A03",
        @RequestParam coordType: String = "WGS84GEO"
    ): TMapReverseGeocodingResponse
}
