package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.domain.BusRouteInfo
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusScheduleProvider
import com.deepromeet.atcha.transit.domain.ServiceRegion
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
@Order(1)
class RegionBusScheduleProvider(
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>
) : BusScheduleProvider {
    override fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule? {
        try {
            val busSchedule =
                busRouteInfoClientMap[routeInfo.route.serviceRegion]?.getBusSchedule(
                    routeInfo
                )
            return busSchedule
        } catch (e: Exception) {
            log.debug(e) {
                "공공데이터에서 ${routeInfo.route.serviceRegion} 막차 정보 조회 실패 - " +
                    "정류장: ${routeInfo.getTargetStation().stationId} 노선: ${routeInfo.route.id}"
            }
            return null
        }
    }
}
