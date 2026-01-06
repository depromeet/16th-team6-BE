package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayRealtimeResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicSubwayRealtimeArrivalHttpClient {
    @GetExchange("/{key}/{type}/realtimeStationArrival/{startIndex}/{endIndex}/{statnNm}")
    suspend fun getPublicSubwayRealtime(
        @PathVariable key: String,
        @PathVariable type: String = "json",
        @PathVariable startIndex: Int = 1,
        @PathVariable endIndex: Int = 10,
        @PathVariable statnNm: String
    ): PublicSubwayRealtimeResponse
}
