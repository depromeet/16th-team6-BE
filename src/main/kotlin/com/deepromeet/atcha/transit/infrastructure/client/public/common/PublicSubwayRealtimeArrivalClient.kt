package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.application.subway.RealtimeSubwayFetcher
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayRealtimeResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicSubwayRealtimeArrivalClient(
    private val publicSubwayRealtimeArrivalHttpClient: PublicSubwayRealtimeArrivalHttpClient,
    @Value("\${open-api.api.subway-realtime-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.subway-realtime-key}")
    private val spareKey: String,
    @Value("\${open-api.api.subway-realtime-key}")
    private val realLastKey: String
) : RealtimeSubwayFetcher {
    override suspend fun fetch(stationName: String): PublicSubwayRealtimeResponse {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key ->
                publicSubwayRealtimeArrivalHttpClient.getPublicSubwayRealtime(key, statnNm = stationName)
            },
            isLimitExceeded = { response -> isRealtimeSubwayApiLimitExceeded(response) },
            processResult = { response -> response },
            errorMessage = "실시간 지하철 도착 정보를 가져오는데 실패했습니다 - $stationName"
        )
    }

    private fun isRealtimeSubwayApiLimitExceeded(response: PublicSubwayRealtimeResponse): Boolean {
        val limitMessages =
            listOf(
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR",
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR"
            )
        return response.errorMessage.code in limitMessages
    }
}
