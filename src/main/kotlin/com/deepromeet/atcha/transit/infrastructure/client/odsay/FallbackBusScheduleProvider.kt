package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusScheduleProvider
import com.deepromeet.atcha.transit.domain.BusStation
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

val log = KotlinLogging.logger {}

@Component
@Order(2)
class FallbackBusScheduleProvider(
    private val odSayBusInfoClient: ODSayBusInfoClient
) : BusScheduleProvider {
    override fun getBusSchedule(
        station: BusStation,
        route: BusRoute
    ): BusSchedule? {
        log.debug { "ODSay를 통한 버스 도착 정보 조회 시도: $station, 노선: $route" }
        return odSayBusInfoClient.getBusSchedule(
            station,
            route
        )
    }
}
