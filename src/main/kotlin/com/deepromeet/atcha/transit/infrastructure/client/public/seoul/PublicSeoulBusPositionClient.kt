package com.deepromeet.atcha.transit.infrastructure.client.public.seoul

import com.deepromeet.atcha.transit.application.bus.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.deepromeet.atcha.transit.domain.bus.BusRouteId
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils.isServiceResultApiLimitExceeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicSeoulBusPositionClient(
    private val publicSeoulBusPositionClient: PublicSeoulBusPositionFeignClient,
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
                apiCall = { key -> publicSeoulBusPositionClient.getBusPosByRtid(key, routeId.value) },
                isLimitExceeded = { response -> isServiceResultApiLimitExceeded(response) },
                processResult = { response ->
                    response.msgBody.itemList?.map { busPositionResponse ->
                        busPositionResponse.toBusPosition()
                    } ?: emptyList()
                },
                errorMessage = "서울 버스 위치 정보를 가져오는데 실패했습니다."
            )
        }
}
