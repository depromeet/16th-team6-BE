package com.deepromeet.atcha.mixpanel

import com.deepromeet.atcha.mixpanel.event.BusApiCallCountPerRequestProperty
import com.deepromeet.atcha.mixpanel.event.ODsayCallRouteProperty
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class MixpanelEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    fun publishODsayCallRouteEvent(routeInfo: BusRouteInfo) {
        applicationEventPublisher.publishEvent(
            MixpanelEvent(
                mixpanelEventName = MixpanelEventName.ODSAY_CALL_COUNT,
                distinctId = routeInfo.route.id.value,
                property =
                    ODsayCallRouteProperty(
                        serviceRegion = routeInfo.route.serviceRegion.regionName,
                        startStationName = routeInfo.passStopList.busRouteStations.first().stationName,
                        endStationName = routeInfo.passStopList.busRouteStations.last().stationName
                    )
            )
        )
    }

    fun publishBusRouteApiCallCountEvent(property: BusApiCallCountPerRequestProperty) {
        applicationEventPublisher.publishEvent(
            MixpanelEvent(
                mixpanelEventName = MixpanelEventName.BUS_API_CALL_COUNT_PER_REQUEST,
                distinctId = property.routeName,
                property = property
            )
        )
    }
}
