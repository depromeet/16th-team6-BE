package com.deepromeet.atcha.transit.domain.subway

import com.deepromeet.atcha.transit.domain.DailyTypeResolver
import com.deepromeet.atcha.transit.domain.TransitType
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
    private val subwayTimeTableCache: SubwayTimeTableCache
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
        return withContext(Dispatchers.IO) {
            subwayStationRepository.findStationByNameAndRoute(subwayLine.lnCd, stationName)
                ?: throw TransitException.of(
                    TransitError.NOT_FOUND_SUBWAY_STATION,
                    "지하철 노선 '${subwayLine.name}'에서 역 '$stationName'을 찾을 수 없습니다."
                )
        }
    }

    suspend fun getTimeTable(
        startStation: SubwayStation,
        endStation: SubwayStation,
        routes: List<Route>
    ): SubwayTimeTable {
        val dailyType = dailyTypeResolver.resolve(TransitType.SUBWAY)
        val direction = SubwayDirection.resolve(routes, startStation, endStation)

        subwayTimeTableCache.get(startStation, dailyType, direction)?.let {
            return it
        }

        return subwayTimetableClient.getTimeTable(startStation, dailyType, direction).also { timeTable ->
            subwayTimeTableCache.cache(startStation, dailyType, direction, timeTable)
        }
    }
}
