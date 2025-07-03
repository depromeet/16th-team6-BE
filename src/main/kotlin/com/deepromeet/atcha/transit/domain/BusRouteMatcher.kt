package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.PassStopList
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
        passStopList: PassStopList
    ): BusRouteInfo {
        val plannedStops = passStopList.stationList.map { it.stationName }
        val similarities = mutableListOf<Double>()

        busRoutes.forEach { route ->
            val stationList = busRouteInfoClientMap[route.serviceRegion]!!.getStationList(route)
            val routeStops = stationList.busRouteStations.map { it.stationName }

            val startIdxs =
                routeStops
                    .withIndex()
                    .filter { transitNameComparer.isSame(it.value, stationMeta.name) }
                    .map { it.index }

            if (startIdxs.isEmpty()) return@forEach

            for (curIdx in startIdxs) {
                val end = minOf(curIdx + plannedStops.size + SLACK, routeStops.size)
                val window = routeStops.subList(curIdx, end)

                val lcs = lcsLength(plannedStops, window)
                val similarity = lcs.toDouble() / plannedStops.size

                similarities.add(similarity)

                if (similarity >= SIM_THRESHOLD) {
                    return BusRouteInfo(route, curIdx, stationList)
                }
            }
        }

        throw TransitException.of(
            TransitError.NOT_FOUND_BUS_ROUTE,
            "'${stationMeta.name}'을 경유하며 사용자 경로와 유사한 노선을 찾지 못했습니다. " +
                "노선 유사도: ${similarities.joinToString(", ")}"
        )
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
}
