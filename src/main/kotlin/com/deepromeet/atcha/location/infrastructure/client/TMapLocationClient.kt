package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.infrastructure.client.response.TMapPOIResponse
import com.deepromeet.atcha.location.infrastructure.client.response.TMapReverseLabelResponse
import com.deepromeet.atcha.transit.infrastructure.client.TMapFeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

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
}
