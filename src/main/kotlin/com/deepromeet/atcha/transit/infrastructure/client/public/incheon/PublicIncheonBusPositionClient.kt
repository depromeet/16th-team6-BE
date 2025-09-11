package com.deepromeet.atcha.transit.infrastructure.client.public.incheon

import com.deepromeet.atcha.transit.application.bus.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.deepromeet.atcha.transit.domain.bus.BusRouteId
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicIncheonBusPositionClient(
    private val publicIncheonBusPositionHttpClient: PublicIncheonBusPositionHttpClient,
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
            apiCall = { key ->
                publicIncheonBusPositionHttpClient.getBusRouteLocation(
                    serviceKey = key,
                    routeId = routeId.value
                )
            },
            isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody.itemList?.map { it.toBusPosition() }
                    ?: emptyList()
            },
            errorMessage = "인천시 버스 위치 정보를 가져오는데 실패했습니다."
        )
}
