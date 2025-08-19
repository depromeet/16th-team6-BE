package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.shared.infrastructure.mixpanel.MixpanelEventPublisher
import com.deepromeet.atcha.shared.infrastructure.mixpanel.event.BusApiCallCountPerRequestProperty
import com.deepromeet.atcha.transit.application.bus.BusScheduleProvider
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

val log = KotlinLogging.logger {}

@Component
@Order(2)
class FallbackBusScheduleProvider(
    private val odSayBusInfoClient: ODSayBusInfoClient,
    val mixpanelEventPublisher: MixpanelEventPublisher
) : BusScheduleProvider {
    override suspend fun getBusSchedule(
        routeInfo: BusRouteInfo,
        busApiCallCountPerRequestProperty: BusApiCallCountPerRequestProperty
    ): BusSchedule? {
        log.debug { "ODSay를 통한 버스 도착 정보 조회 시도: ${routeInfo.getTargetStation().busStation}, 노선: ${routeInfo.route}" }
        try {
            busApiCallCountPerRequestProperty.incrementODsayCallCount()
            mixpanelEventPublisher.publishODsayCallRouteEvent(routeInfo)

            return odSayBusInfoClient.getBusSchedule(routeInfo)
        } catch (e: Exception) {
            throw e
        }
    }
}
