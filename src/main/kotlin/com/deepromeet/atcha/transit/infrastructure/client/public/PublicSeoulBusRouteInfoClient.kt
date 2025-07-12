package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRealTimeArrival
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.infrastructure.client.public.response.toBusRouteOperationInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicSeoulBusRouteInfoClient(
    private val publicSeoulBusArrivalInfoFeignClient: PublicSeoulBusArrivalInfoFeignClient,
    private val seoulBusOperationFeignClient: SeoulBusOperationFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : BusRouteInfoClient {
    override fun getBusSchedule(
        station: BusStation,
        route: BusRoute
    ): BusSchedule? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicSeoulBusArrivalInfoFeignClient.getArrivalInfoByRoute(route.id.value, key) },
            isLimitExceeded = { response -> ApiClientUtils.isSeoulApiLimitExceeded(response) },
            processResult = { response ->
                val findResult = response.msgBody.itemList?.find { it.arsId == station.busStationNumber.value }
                findResult?.toBusSchedule(station)
            },
            errorMessage = "서울시 버스 도착 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo? {
        return seoulBusOperationFeignClient.getBusOperationInfo(route.id.value).toBusRouteOperationInfo()
    }

    override fun getBusRealTimeInfo(
        station: BusStation,
        route: BusRoute
    ): BusRealTimeArrival? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicSeoulBusArrivalInfoFeignClient.getArrivalInfoByRoute(route.id.value, key) },
            isLimitExceeded = { response -> ApiClientUtils.isSeoulApiLimitExceeded(response) },
            processResult = { response ->
                val findResult = response.msgBody.itemList?.find { it.arsId == station.busStationNumber.value }
                findResult?.toBusRealTimeArrival()
            },
            errorMessage = "서울시 버스 실시간 정보를 가져오는데 실패했습니다."
        )
    }
}
