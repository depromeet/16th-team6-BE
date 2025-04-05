package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.infrastructure.client.public.ApiClientUtils.isSeoulApiLimitExceeded
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
    override fun fetch(routeId: BusRouteId): List<BusPosition> {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicSeoulBusPositionClient.getBusPosByRtid(key, routeId.value) },
            isLimitExceeded = { response -> isSeoulApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody.itemList?.map { busPositionResponse ->
                    busPositionResponse.toBusPosition()
                } ?: emptyList()
            },
            errorMessage = "서울 버스 위치 정보를 가져오는데 실패했습니다."
        ) ?: emptyList() // API 호출 실패 시 빈 리스트 반환
    }
}
