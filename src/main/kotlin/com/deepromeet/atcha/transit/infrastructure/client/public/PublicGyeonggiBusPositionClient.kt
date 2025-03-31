package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.infrastructure.client.common.ApiClientUtils
import com.deepromeet.atcha.transit.infrastructure.client.common.ApiClientUtils.isGyeonggiApiLimitExceeded
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicGyeonggiBusPositionClient(
    private val publicGyeonggiBusPositionClient: PublicGyeonggiBusPositionFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String
) : BusPositionFetcher {
    override fun fetch(routeId: BusRouteId): List<BusPosition> {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            apiCall = { key -> publicGyeonggiBusPositionClient.getBusLocationList(key, routeId.value) },
            isLimitExceeded = { response -> isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                response.response.msgBody.busLocationList.map { it.toBusPosition() }
            },
            errorMessage = "경기도 버스 위치 정보를 가져오는데 실패했습니다."
        ) ?: emptyList() // API 호출 실패 시 빈 리스트 반환
    }
}
