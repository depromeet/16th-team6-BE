package com.deepromeet.atcha.transit.application.subway

import com.deepromeet.atcha.transit.application.DailyTypeResolver
import com.deepromeet.atcha.transit.domain.TransitType
import com.deepromeet.atcha.transit.domain.subway.Route
import com.deepromeet.atcha.transit.domain.subway.SubwayArrival
import com.deepromeet.atcha.transit.domain.subway.SubwayDirection
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.domain.subway.SubwayRealTimeArrivals
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.deepromeet.atcha.transit.domain.subway.SubwayTimeTable
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayBranchRepository
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class SubwayManager(
    private val subwayStationRepository: SubwayStationRepository,
    private val subwayTimetableClient: SubwayTimetableClient,
    private val dailyTypeResolver: DailyTypeResolver,
    private val subwayBranchRepository: SubwayBranchRepository,
    private val subwayTimeTableCache: SubwayTimeTableCache,
    private val subwayStationCache: SubwayStationCache,
    private val subwayRouteCache: SubwayRouteCache,
    private val realtimeSubwayFetcher: RealtimeSubwayFetcher
) {
    suspend fun getRoutes(subwayLine: SubwayLine): List<Route> {
        return subwayRouteCache.get(subwayLine)
            ?: withContext(Dispatchers.IO) {
                subwayBranchRepository.findByRouteCode(subwayLine.lnCd)
                    .groupBy { it.finalStationName }
                    .values
                    .map { Route(it) }
            }.also { routes -> subwayRouteCache.cache(subwayLine, routes) }
    }

    suspend fun getStation(
        subwayLine: SubwayLine,
        stationName: String
    ): SubwayStation {
        return subwayStationCache.get(subwayLine, stationName)
            ?: withContext(Dispatchers.IO) {
                subwayStationRepository.findStationByNameAndRoute(subwayLine.lnCd, stationName)
                    ?: throw TransitException.of(
                        TransitError.NOT_FOUND_SUBWAY_STATION,
                        "DB에서 지하철 노선 '${subwayLine.mainName()}'의 역 '$stationName'을 찾을 수 없습니다."
                    )
            }.also { subwayStation -> subwayStationCache.cache(subwayLine, stationName, subwayStation) }
    }

    suspend fun getTimeTable(
        start: SubwayStation,
        next: SubwayStation,
        destination: SubwayStation,
        routes: List<Route>,
        isExpress: Boolean
    ): SubwayTimeTable {
        val dailyType = dailyTypeResolver.resolve(TransitType.SUBWAY)
        val direction = SubwayDirection.resolve(routes, start, next, destination)

        val subwayTimeTable =
            subwayTimeTableCache.get(start, dailyType, direction)
                ?: subwayTimetableClient.getTimeTable(start, dailyType, direction)
                    .also { timeTable -> subwayTimeTableCache.cache(start, dailyType, direction, timeTable) }

        return subwayTimeTable.filterReachable(destination, routes, isExpress)
    }

    suspend fun getRealTimeSubwayArrivals(
        stationName: String,
        subwayLine: SubwayLine,
        direction: SubwayDirection
    ): SubwayRealTimeArrivals {
        val stationName = stationName.replace("역$".toRegex(), "")
        val response = realtimeSubwayFetcher.fetch(stationName)

        if (response.realtimeArrivalList.isNullOrEmpty()) {
            return SubwayRealTimeArrivals.empty()
        }

        val filteredArrivals =
            response.realtimeArrivalList
                .filter { arrival ->
                    matchesSubwayLine(arrival.subwayId, subwayLine) &&
                        matchesDirection(arrival.updnLine, direction)
                }
                .map { SubwayArrival.fromRealtimeArrival(it) }
                .sortedBy { it.remainingTimeSeconds }

        return SubwayRealTimeArrivals(filteredArrivals)
    }

    private fun matchesSubwayLine(
        subwayId: String,
        targetLine: SubwayLine
    ): Boolean {
        val lineCode =
            when (subwayId) {
                "1001" -> "1"
                "1002" -> "2"
                "1003" -> "3"
                "1004" -> "4"
                "1005" -> "5"
                "1006" -> "6"
                "1007" -> "7"
                "1008" -> "8"
                "1009" -> "9"
                "1063" -> "K4"
                "1065" -> "A1"
                "1067" -> "K1"
                "1075" -> "K2"
                "1077" -> "D1"
                "1092" -> "UI"
                "1093" -> "WS"
                "1081" -> "K5"
                "1094" -> "L1"
                else -> subwayId
            }
        return lineCode == targetLine.lnCd
    }

    private fun matchesDirection(
        updnLine: String,
        targetDirection: SubwayDirection
    ): Boolean {
        return when (updnLine) {
            "상행", "내선" -> targetDirection == SubwayDirection.UP
            "하행", "외선" -> targetDirection == SubwayDirection.DOWN
            else -> true
        }
    }
}
