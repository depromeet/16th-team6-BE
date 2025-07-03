package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayJsonResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.TrainScheduleResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-subway-schedule-client",
    url = "\${open-api.api.url.subway-schedule}",
    configuration = [PublicFeignConfig::class]
)
interface PublicSubwayScheduleFeignClient {
    @GetMapping("/getTrainSch")
    fun getTrainSchedule(
        @RequestParam serviceKey: String,
        @RequestParam lineNm: String,
        @RequestParam stnNm: String,
        @RequestParam wkndSe: String,
        @RequestParam upbdnbSe: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 200,
        @RequestParam dataType: String = "JSON",
        @RequestParam tmprTmtblYn: String = "N"
    ): PublicSubwayJsonResponse<List<TrainScheduleResponse>>
}
