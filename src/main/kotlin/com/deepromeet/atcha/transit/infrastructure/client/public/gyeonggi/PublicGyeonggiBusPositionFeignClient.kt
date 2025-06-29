package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.GyeonggiBusLocationListResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.PublicGyeonggiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-gyeonggi-bus-position",
    url = "\${open-api.api.url.gyeonggi-bus-position}",
    configuration = [PublicFeignConfig::class]
)
interface PublicGyeonggiBusPositionFeignClient {
    @GetMapping("/getBusLocationListv2")
    fun getBusLocationList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam format: String = "xml"
    ): PublicGyeonggiResponse<GyeonggiBusLocationListResponse>
}
