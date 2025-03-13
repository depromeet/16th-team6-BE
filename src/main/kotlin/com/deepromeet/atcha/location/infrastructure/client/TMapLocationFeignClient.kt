package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.infrastructure.client.response.TMapPOIResponse
import com.deepromeet.atcha.location.infrastructure.client.response.TMapReverseLabelResponse
import com.deepromeet.atcha.transit.infrastructure.client.tmap.config.TMapFeignConfig
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
    ): TMapPOIResponse

    @GetMapping("/tmap/geo/reverseLabel")
    fun getReverseGeoLabel(
        @RequestParam centerLat: Double,
        @RequestParam centerLon: Double,
        @RequestParam reqCoordType: String = "WGS84GEO",
        @RequestParam resCoordType: String = "WGS84GEO",
        @RequestParam reqLevel: Int = 19
    ): TMapReverseLabelResponse
}
