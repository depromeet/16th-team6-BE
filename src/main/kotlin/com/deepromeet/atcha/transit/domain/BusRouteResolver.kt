package com.deepromeet.atcha.transit.domain

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
    fun resolve(
        routeName: String,
        station: BusStationMeta,
        nextStationName: String?
    ): BusRouteInfo {
        val candidateRegions = regionPolicy.candidates(station)

        candidateRegions.forEach { region ->
            tryFetch(region, routeName, station, nextStationName)?.let { return it }
        }

        throw TransitException.of(
            TransitError.NOT_FOUND_BUS_ROUTE,
            "버스 노선 '$routeName' 을 $candidateRegions 모든 후보 지역에서 찾지 못했습니다."
        )
    }

    private fun tryFetch(
        region: ServiceRegion,
        routeName: String,
        station: BusStationMeta,
        nextStationName: String?
    ): BusRouteInfo? =
        runCatching {
            val routes = clientMap[region]!!.getBusRoute(routeName)
            busRouteMatcher.getMatchedRoute(routes, station, nextStationName)
        }.onFailure { e ->
            log.info(e) { "[$region] 버스 노선 '$routeName' 조회 실패" }
        }.getOrNull()
}
