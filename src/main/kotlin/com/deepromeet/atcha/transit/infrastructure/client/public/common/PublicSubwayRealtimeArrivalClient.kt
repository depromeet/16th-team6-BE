package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.application.subway.RealtimeSubwayFetcher
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayRealtimeResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicSubwayRealtimeArrivalClient(
    private val publicSubwayRealtimeArrivalHttpClient: PublicSubwayRealtimeArrivalHttpClient,
    @Value("\${open-api.api.realtime-subway.key}")
    private val serviceKey: String,
    @Value("\${open-api.api.realtime-subway.key}")
    private val spareKey: String,
    @Value("\${open-api.api.realtime-subway.key}")
    private val realLastKey: String
) : RealtimeSubwayFetcher {
    override suspend fun fetch(statnNm: String) {
        val result =
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = {
                        key ->
                    publicSubwayRealtimeArrivalHttpClient.getPublicSubwayRealtime(key, statnNm = statnNm)
                },
                isLimitExceeded = { response -> isRealtimeSubwayApiLimitExceeded(response) },
                processResult = { response ->
                    response
                },
                errorMessage = "실시간 지하철 호선 정보를 가져오는데 실패했습니다 - $statnNm"
            )
        println(result)
    }

    private fun isRealtimeSubwayApiLimitExceeded(response: PublicSubwayRealtimeResponse): Boolean {
        val limitMessages =
            listOf(
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR",
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR"
            )

//        val isLimited =
//            response.header.resultCode != "00" ||
//                (limitMessages.any { response.header.resultMsg.contains(it) })
//
//        if (isLimited) {
//            log.warn { "공휴일 API 요청 수 초과: ${response.header.resultMsg}" }
//        }
//
//        return isLimited
        return false
    }
}
