package com.deepromeet.atcha.transit.application.subway

import com.deepromeet.atcha.transit.application.DailyTypeResolver
import com.deepromeet.atcha.transit.domain.TransitType
import com.deepromeet.atcha.transit.domain.subway.Route
import com.deepromeet.atcha.transit.domain.subway.SubwayDirection
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
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
    private val subwayStationCache: SubwayStationCache
) {
    suspend fun getRoutes(subwayLine: SubwayLine): List<Route> =
        withContext(Dispatchers.IO) {
            subwayBranchRepository.findByRouteCode(subwayLine.lnCd)
                .groupBy { it.finalStationName }
                .values
                .map { Route(it) }
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
        startStation: SubwayStation,
        nextStation: SubwayStation,
        endStation: SubwayStation,
        routes: List<Route>
    ): SubwayTimeTable {
        val dailyType = dailyTypeResolver.resolve(TransitType.SUBWAY)
        val direction = SubwayDirection.resolve(routes, startStation, nextStation, endStation)

        val subwayTimeTable =
            subwayTimeTableCache.get(startStation, dailyType, direction)
                ?: subwayTimetableClient.getTimeTable(startStation, dailyType, direction).also { timeTable ->
                    subwayTimeTableCache.cache(startStation, dailyType, direction, timeTable)
                }

        return subwayTimeTable.filterReachable(endStation, routes)
    }
}
