package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.location.domain.POIFinder
import org.springframework.stereotype.Component

@FeignClient(
    name = "tmap-location",
    url = "\${tmap.api.url}",
    configuration = [TMapFeignConfig::class]
)
interface TMapLocationClient {
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
