package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.PassStopList
import io.github.oshai.kotlinlogging.KotlinLogging
import jdk.jfr.internal.OldObjectSample.emit
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
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
        passStopList: PassStopList
    ): BusRouteInfo {
        val candidateRegions = regionPolicy.candidates(station)

        val firstResult =
            candidateRegions.asFlow()
                .flatMapMerge(concurrency = candidateRegions.size) { region ->
                    flow {
                        emit(tryFetch(region, routeName, station, passStopList))
                    }
                }
                .filterNotNull()
                .firstOrNull()

        return firstResult ?: throw TransitException.of(
            TransitError.NOT_FOUND_BUS_ROUTE,
            "버스 노선 '$routeName' 을 $candidateRegions 모든 후보 지역에서 찾지 못했습니다."
        )
    }

    private suspend fun tryFetch(
        region: ServiceRegion,
        routeName: String,
        station: BusStationMeta,
        passStopList: PassStopList
    ): BusRouteInfo? =
        runCatching {
            val routes = clientMap[region]!!.getBusRoute(routeName)
            busRouteMatcher.getMatchedRoute(routes, station, passStopList)
        }.onFailure { e ->
            log.debug(e) { "[$region] 버스 노선 '$routeName' 조회 실패" }
        }.getOrNull()
}
