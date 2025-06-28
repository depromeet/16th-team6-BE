package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.domain.BusRouteInfo
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusScheduleProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

val log = KotlinLogging.logger {}

@Component
@Order(2)
class FallbackBusScheduleProvider(
    private val odSayBusInfoClient: ODSayBusInfoClient
) : BusScheduleProvider {
    override fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule? {
        log.info { "ODSay를 통한 버스 도착 정보 조회 시도: ${routeInfo.getTargetStation().busStation}, 노선: ${routeInfo.route}" }
        try {
            return odSayBusInfoClient.getBusSchedule(routeInfo)
        } catch (e: Exception) {
            log.warn(
                e
            ) { "ODSay를 통한 버스 도착 정보 조회 실패: ${routeInfo.getTargetStation().busStation}, 노선: ${routeInfo.route}" }
            throw e
        }
    }
}
