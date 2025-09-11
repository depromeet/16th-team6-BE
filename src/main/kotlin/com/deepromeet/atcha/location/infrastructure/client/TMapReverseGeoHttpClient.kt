package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.infrastructure.client.response.TMapAddressResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface TMapReverseGeoHttpClient {
    @GetExchange("/tmap/geo/reversegeocoding")
    fun getReverseGeo(
        @RequestParam lat: String,
        @RequestParam lon: String
    ): TMapAddressResponse
}
