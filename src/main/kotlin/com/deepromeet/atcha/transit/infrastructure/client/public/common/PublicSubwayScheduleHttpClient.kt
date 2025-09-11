package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayJsonResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.TrainScheduleResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicSubwayScheduleHttpClient {
    @GetExchange("/getTrainSch")
    suspend fun getTrainSchedule(
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
