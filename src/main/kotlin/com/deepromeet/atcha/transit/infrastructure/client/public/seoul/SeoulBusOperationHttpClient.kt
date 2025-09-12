package com.deepromeet.atcha.transit.infrastructure.client.public.seoul

import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response.SeoulBusOperationResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface SeoulBusOperationHttpClient {
    @GetExchange
    suspend fun getBusOperationInfo(
        @RequestParam routId: String
    ): SeoulBusOperationResponse
}
