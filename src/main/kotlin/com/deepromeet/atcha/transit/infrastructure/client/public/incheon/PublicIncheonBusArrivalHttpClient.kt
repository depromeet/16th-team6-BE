package com.deepromeet.atcha.transit.infrastructure.client.public.incheon

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response.IncheonBusArrivalResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicIncheonBusArrivalHttpClient {
    @GetExchange("/getBusArrivalList")
    suspend fun getBusArrivalList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam bstopId: String,
        @RequestParam numOfRows: Int = 1000,
        @RequestParam pageNo: Int = 1
    ): ServiceResult<IncheonBusArrivalResponse>
}
