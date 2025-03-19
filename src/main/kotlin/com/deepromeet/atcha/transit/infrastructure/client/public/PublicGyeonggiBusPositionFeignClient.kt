package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.common.feign.FeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiApiResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-gyeonggi-bus-position",
    url = "\${open-api.api.url.gyeonggi-bus-position}",
    configuration = [FeignConfig::class]
)
interface PublicGyeonggiBusPositionFeignClient {
    @GetMapping("/getBusLocationListv2")
    fun getBusLocationList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam format: String = "json"
    ): PublicGyeonggiApiResponse<PublicGyeonggiResponse.BusLocationListResponse>
}
