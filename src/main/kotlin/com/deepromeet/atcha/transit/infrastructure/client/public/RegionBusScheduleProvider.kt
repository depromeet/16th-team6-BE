package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusScheduleProvider
import com.deepromeet.atcha.transit.domain.BusStation
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
class RegionBusScheduleProvider(
    private val busRouteInfoClientMap: Map<String, BusRouteInfoClient>
) : BusScheduleProvider {
    override fun getBusSchedule(
        station: BusStation,
        route: BusRoute
    ): BusSchedule? {
        return busRouteInfoClientMap[route.serviceRegion.name]?.getBusSchedule(
            station,
            route
        )
    }
}
