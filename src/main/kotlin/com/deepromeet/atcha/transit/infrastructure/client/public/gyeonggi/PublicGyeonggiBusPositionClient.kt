package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi

import com.deepromeet.atcha.transit.application.bus.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.deepromeet.atcha.transit.domain.bus.BusRouteId
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils.isGyeonggiApiLimitExceeded
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicGyeonggiBusPositionClient(
    private val publicGyeonggiBusPositionHttpClient: PublicGyeonggiBusPositionHttpClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : BusPositionFetcher {
    override suspend fun fetch(routeId: BusRouteId): List<BusPosition> =
        ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiBusPositionHttpClient.getBusLocationList(key, routeId.value) },
            isLimitExceeded = { response -> isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody?.busLocationList
                    ?.map { it.toBusPosition() }
                    ?: emptyList()
            },
            errorMessage = "경기도 버스 '${routeId.value}'의 버스 위치 정보를 가져오는 데 실패했습니다."
        )
}
