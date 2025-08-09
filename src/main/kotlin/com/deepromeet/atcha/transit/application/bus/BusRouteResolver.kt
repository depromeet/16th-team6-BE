package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.transit.application.region.ServiceRegionCandidatePolicy
import com.deepromeet.atcha.transit.domain.RoutePassStops
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.domain.region.ServiceRegion
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class BusRouteResolver(
    private val clientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val busRouteMatcher: BusRouteMatcher,
    private val regionPolicy: ServiceRegionCandidatePolicy
) {
    suspend fun resolve(
        routeName: String,
        station: BusStationMeta,
        passStopList: RoutePassStops
    ): BusRouteInfo {
        val candidateRegions = regionPolicy.candidates(station)

        for (region in candidateRegions) {
            val result = tryFetch(region, routeName, station, passStopList)
            if (result != null) {
                return result
            }
        }

        throw TransitException.of(
            TransitError.NOT_FOUND_BUS_ROUTE,
            "버스 노선 '$routeName' 을 $candidateRegions 모든 후보 지역에서 찾지 못했습니다."
        )
    }

    private suspend fun tryFetch(
        region: ServiceRegion,
        routeName: String,
        station: BusStationMeta,
        passStopList: RoutePassStops
    ): BusRouteInfo? =
        runCatching {
            val routes = clientMap[region]!!.getBusRoute(routeName)
            busRouteMatcher.getMatchedRoute(routes, station, passStopList)
        }.getOrNull()
}
