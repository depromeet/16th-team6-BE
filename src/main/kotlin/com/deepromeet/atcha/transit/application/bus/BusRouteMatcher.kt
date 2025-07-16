package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.route.domain.RoutePassStops
import com.deepromeet.atcha.transit.application.TransitNameComparer
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.domain.region.ServiceRegion
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

private const val SIM_THRESHOLD = 0.6
private const val SLACK = 5

@Component
class BusRouteMatcher(
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val transitNameComparer: TransitNameComparer
) {
    suspend fun getMatchedRoute(
        busRoutes: List<BusRoute>,
        stationMeta: BusStationMeta,
        passStopList: RoutePassStops
    ): BusRouteInfo =
        coroutineScope {
            val plannedStops = passStopList.stops.map { it.stationName }

            val routeMatching =
                busRoutes.map { route ->
                    async {
                        processRouteWithSimilarity(route, stationMeta, plannedStops)
                    }
                }

            routeMatching.forEach { job ->
                val result = job.await()
                if (result != null) {
                    if (result.similarity >= SIM_THRESHOLD) {
                        return@coroutineScope result.busRouteInfo
                    }
                }
            }

            throw TransitException.of(
                TransitError.NOT_FOUND_BUS_ROUTE,
                "'${stationMeta.name}'을 경유하며 사용자 경로와 유사한 노선을 찾지 못했습니다."
            )
        }

    private suspend fun processRouteWithSimilarity(
        route: BusRoute,
        stationMeta: BusStationMeta,
        plannedStops: List<String>
    ): RouteProcessResult? {
        val stationList = busRouteInfoClientMap[route.serviceRegion]!!.getStationList(route)
        val routeStops = stationList.busRouteStations.map { it.stationName }

        val startIdxs =
            routeStops
                .withIndex()
                .filter { transitNameComparer.isSame(it.value, stationMeta.name) }
                .map { it.index }

        if (startIdxs.isEmpty()) return null

        var bestSimilarity = 0.0
        var bestResult: BusRouteInfo? = null

        for (curIdx in startIdxs) {
            val end = minOf(curIdx + plannedStops.size + SLACK, routeStops.size)
            val window = routeStops.subList(curIdx, end)

            val lcs = lcsLength(plannedStops, window)
            val similarity = lcs.toDouble() / plannedStops.size

            if (similarity > bestSimilarity) {
                bestSimilarity = similarity
                bestResult = BusRouteInfo(route, curIdx, stationList)
            }
        }

        return bestResult?.let {
            RouteProcessResult(
                busRouteInfo = it,
                similarity = bestSimilarity
            )
        }
    }

    private fun lcsLength(
        plannedStops: List<String>,
        window: List<String>
    ): Int {
        val n = plannedStops.size
        val m = window.size
        val dp = Array(n + 1) { IntArray(m + 1) }

        for (i in 1..n) {
            for (j in 1..m) {
                if (transitNameComparer.isSame(plannedStops[i - 1], window[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                } else {
                    dp[i][j] = maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }
        return dp[n][m]
    }

    private data class RouteProcessResult(
        val busRouteInfo: BusRouteInfo,
        val similarity: Double
    )
}
