package com.deepromeet.atcha.transit.infrastructure.client.tmap

import com.deepromeet.atcha.transit.infrastructure.client.tmap.config.TMapFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.TMapAddressResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "tmap-reverse-geo",
    url = "\${tmap.api.url}",
    configuration = [TMapFeignConfig::class]
)
interface TMapReverseGeoFeignClient {
    @GetMapping("/tmap/geo/reversegeocoding")
    fun getReverseGeo(
        @RequestParam lat: String,
        @RequestParam lon: String
    ): TMapAddressResponse
}
