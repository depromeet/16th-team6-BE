package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.application.TransitNameComparer
import com.deepromeet.atcha.transit.application.bus.BusRouteInfoClient.Companion.isValidStation
import com.deepromeet.atcha.transit.domain.RoutePassStops
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

private const val SIM_THRESHOLD = 0.6

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

            val routeMatchingJobs =
                busRoutes.map { route ->
                    async {
                        processRouteWithSimilarity(route, plannedStops)
                    }
                }

            routeMatchingJobs.forEach { job ->
                val result = job.await()
                if (result != null) {
                    coroutineContext.cancelChildren()
                    return@coroutineScope result.busRouteInfo
                }
            }

            throw TransitException.of(
                TransitError.NOT_FOUND_BUS_ROUTE,
                "'${busRoutes.first().serviceRegion}'의 '${busRoutes.map { it.name }}'노선들에서 '${stationMeta.name}'을" +
                    " 경유하며 사용자 경로(${passStopList.stops.map { it.stationName }})와 유사한 노선을 찾지 못했습니다."
            )
        }

    private suspend fun processRouteWithSimilarity(
        route: BusRoute,
        plannedStops: List<String>
    ): RouteProcessResult? {
        val stationList = busRouteInfoClientMap[route.serviceRegion]!!.getStationList(route)
        val routeStations = stationList.busRouteStations.filter(::isValidStation)
        val routeStops = routeStations.map { it.stationName }

        val (lcs, matchingInfo) = lcsWithFirstMatch(plannedStops, routeStops)
        val similarity = lcs.toDouble() / plannedStops.size

        return if (similarity >= SIM_THRESHOLD && matchingInfo != null) {
            val targetStationIndex =
                (matchingInfo.routeStartIndex - matchingInfo.plannedStartIndex)
                    .coerceAtLeast(0)
            val targetStation = routeStations[targetStationIndex]

            RouteProcessResult(
                busRouteInfo = BusRouteInfo(route, targetStation, stationList),
                similarity = similarity
            )
        } else {
            null
        }
    }

    private fun lcsWithFirstMatch(
        plannedStops: List<String>,
        routeStops: List<String>
    ): Pair<Int, MatchingInfo?> {
        val n = plannedStops.size
        val m = routeStops.size

        // 1. DP 테이블 구성
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in 1..n) {
            for (j in 1..m) {
                if (transitNameComparer.isSame(plannedStops[i - 1], routeStops[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                } else {
                    dp[i][j] = maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }
        val maxLcs = dp[n][m]

        // 2. 역추적을 통해 실제 LCS 경로 찾기
        val lcsPath = mutableListOf<Pair<Int, Int>>()

        var i = n
        var j = m
        while (i > 0 && j > 0) {
            if (transitNameComparer.isSame(plannedStops[i - 1], routeStops[j - 1])) {
                lcsPath.add(Pair(i - 1, j - 1)) // 실제 인덱스로 저장
                i--
                j--
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--
            } else {
                j--
            }
        }

        // 3. 경로를 뒤집어서 첫 번째 매칭 지점 반환
        lcsPath.reverse()

        return if (lcsPath.isNotEmpty()) {
            val firstMatch = lcsPath.first()
            Pair(maxLcs, MatchingInfo(firstMatch.first, firstMatch.second))
        } else {
            Pair(maxLcs, null)
        }
    }

    private data class RouteProcessResult(
        val busRouteInfo: BusRouteInfo,
        val similarity: Double
    )

    private data class MatchingInfo(
        val plannedStartIndex: Int,
        val routeStartIndex: Int
    )
}
