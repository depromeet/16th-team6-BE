package com.deepromeet.atcha.transit.infrastructure.client.public.incheon

import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.deepromeet.atcha.transit.domain.bus.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.bus.BusRouteId
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicIncheonBusPositionClient(
    private val publicIncheonBusPositionFeignClient: PublicIncheonBusPositionFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : BusPositionFetcher {
    override suspend fun fetch(routeId: BusRouteId): List<BusPosition> =
        withContext(Dispatchers.IO) {
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key ->
                    publicIncheonBusPositionFeignClient.getBusRouteLocation(
                        serviceKey = key,
                        routeId = routeId.value
                    )
                },
                isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
                processResult = { response ->
                    response.msgBody.itemList?.map { it.toBusPosition() }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_POSITION,
                            "인천시 버스 노선 ID '${routeId.value}'에 해당하는 버스 위치 정보를 찾을 수 없습니다."
                        )
                },
                errorMessage = "인천시 버스 위치 정보를 가져오는데 실패했습니다."
            )
        }
}
