package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.IncheonBusArrivalResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-incheon-bus-arrival-feign-client",
    url = "\${open-api.api.url.incheon-arrival}",
    configuration = [PublicFeignConfig::class]
)
interface PublicIncheonBusArrivalFeignClient {
    @GetMapping("/getBusArrivalList")
    fun getBusArrivalList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam bstopId: String,
        @RequestParam numOfRows: Int = 1000,
        @RequestParam pageNo: Int = 1
    ): ServiceResult<IncheonBusArrivalResponse>
}
