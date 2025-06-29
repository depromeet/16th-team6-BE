package com.deepromeet.atcha.transit.infrastructure.client.public

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
            log.debug { "공공데이터에서 막차 정보 조회 성공 - 정류장: ${routeInfo.getTargetStation().stationName} 노선: $routeInfo" }
            return busSchedule
        } catch (e: Exception) {
            log.debug(e) { "공공 데이터 버스 시간표 조회 실패 - 정류장: ${routeInfo.getTargetStation().stationName} 노선: $routeInfo" }
            return null
        }
    }
}
