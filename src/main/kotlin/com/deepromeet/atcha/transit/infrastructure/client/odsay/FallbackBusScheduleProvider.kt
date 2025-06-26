package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusScheduleProvider
import com.deepromeet.atcha.transit.domain.BusStation
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(2)
class FallbackBusScheduleProvider(
    private val odSayBusInfoClient: ODSayBusInfoClient
) : BusScheduleProvider {
    override fun getBusSchedule(
        station: BusStation,
        route: BusRoute
    ): BusSchedule? {
        return odSayBusInfoClient.getBusSchedule(
            station,
            route
        )
    }
}
