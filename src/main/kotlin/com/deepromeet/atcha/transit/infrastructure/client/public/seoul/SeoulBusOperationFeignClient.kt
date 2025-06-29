package com.deepromeet.atcha.transit.infrastructure.client.public.seoul

import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response.SeoulBusOperationResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-seoul-bus-operation",
    url = "\${open-api.api.url.bus-operation}"
)
interface SeoulBusOperationFeignClient {
    @GetMapping
    fun getBusOperationInfo(
        @RequestParam routId: String
    ): SeoulBusOperationResponse
}
