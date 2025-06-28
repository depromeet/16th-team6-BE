package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.ApiClientUtils.isServiceResultApiLimitExceeded
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
            isLimitExceeded = { response -> isServiceResultApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody.itemList?.map { busPositionResponse ->
                    busPositionResponse.toBusPosition()
                } ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_POSITION,
                    "서울 버스 노선 '${routeId.value}'의 버스 위치 정보를 찾을 수 없습니다."
                )
            },
            errorMessage = "서울 버스 위치 정보를 가져오는데 실패했습니다."
        )
    }
}
