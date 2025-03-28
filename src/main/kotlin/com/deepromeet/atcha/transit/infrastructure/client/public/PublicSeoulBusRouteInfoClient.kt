package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.infrastructure.client.public.response.toBusRouteOperationInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicSeoulBusRouteInfoClient(
    private val publicSeoulBusArrivalInfoFeignClient: PublicSeoulBusArrivalInfoFeignClient,
    private val seoulBusOperationFeignClient: SeoulBusOperationFeignClient
) : BusRouteInfoClient {
    override fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival? {
        try {
            val response =
                publicSeoulBusArrivalInfoFeignClient.getArrivalInfoByRoute(route.id.value)
            val findResult = response.msgBody.itemList?.find { it.arsId == station.id.value }
            return findResult?.toBusArrival() ?: return null
        } catch (e: Exception) {
            log.warn(e) { "서울시 버스 도착 정보를 가져오는데 실패했습니다." }
            return null
        }
    }

    override fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo? {
        return seoulBusOperationFeignClient.getBusOperationInfo(route.id.value).toBusRouteOperationInfo()
    }
}
