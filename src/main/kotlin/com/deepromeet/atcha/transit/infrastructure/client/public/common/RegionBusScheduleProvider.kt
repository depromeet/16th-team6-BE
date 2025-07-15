package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusScheduleProvider
import com.deepromeet.atcha.transit.domain.region.ServiceRegion
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
@Order(1)
class RegionBusScheduleProvider(
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>
) : BusScheduleProvider {
    override suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule? {
        try {
            return busRouteInfoClientMap[routeInfo.route.serviceRegion]
                ?.getBusSchedule(routeInfo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.debug(e) {
                "공공데이터에서 ${routeInfo.route.serviceRegion} 막차 정보 조회 실패 - " +
                    "정류장: ${routeInfo.getTargetStation().stationId} 노선: ${routeInfo.route.id}"
            }
            return null
        }
    }
}
