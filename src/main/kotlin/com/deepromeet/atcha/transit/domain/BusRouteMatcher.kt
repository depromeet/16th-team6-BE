package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import org.springframework.stereotype.Component

@Component
class BusRouteMatcher(
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val transitNameComparer: TransitNameComparer
) {
    fun getMatchedRoute(
        busRoutes: List<BusRoute>,
        stationMeta: BusStationMeta,
        nextStationName: String?
    ): BusRouteInfo {
        busRoutes.forEach { rt ->

            val routeStationList = busRouteInfoClientMap[rt.serviceRegion]!!.getStationList(rt)

            val passStops = routeStationList.busRouteStations

            passStops.forEachIndexed { idx, station ->
                if (!transitNameComparer.isSame(station.stationName, stationMeta.name)) {
                    return@forEachIndexed
                }

                if (nextStationName == null) {
                    return BusRouteInfo(rt, idx, routeStationList)
                }

                val targetNextStation = passStops[idx + 1].stationName
                if (idx < passStops.lastIndex &&
                    transitNameComparer.isSame(targetNextStation, nextStationName)
                ) {
                    return BusRouteInfo(rt, idx, routeStationList)
                }
            }
        }

        throw TransitException.of(
            TransitError.NOT_FOUND_BUS_ROUTE,
            "'${stationMeta.name} -> $nextStationName'을" +
                " 경유하는 ${busRoutes[0].serviceRegion} 버스노선 '${busRoutes[0].name}'을 찾을 수 없습니다."
        )
    }
}
