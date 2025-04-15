package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayBranchRepository
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import org.springframework.stereotype.Component

@Component
class SubwayManager(
    private val subwayStationRepository: SubwayStationRepository,
    private val subwayInfoClient: SubwayInfoClient,
    private val dailyTypeResolver: DailyTypeResolver,
    private val subwayBranchRepository: SubwayBranchRepository,
    private val subwayTimeTableCache: SubwayTimeTableCache
) {
    fun getRoutes(subwayLine: SubwayLine) =
        subwayBranchRepository.findByRouteCode(subwayLine.lnCd)
            .groupBy { it.finalStationName }
            .values
            .map { Route(it) }

    fun getStation(
        subwayLine: SubwayLine,
        stationName: String
    ): SubwayStation {
        return subwayStationRepository.findByRouteCodeAndNameOrLike(subwayLine.lnCd, stationName)
            ?: throw TransitException.NotFoundSubwayStation
    }

    fun getTimeTable(
        startStation: SubwayStation,
        endStation: SubwayStation,
        routes: List<Route>
    ): SubwayTimeTable? {
        val dailyType = dailyTypeResolver.resolve()
        val direction = SubwayDirection.resolve(routes, startStation, endStation)

//        subwayTimeTableCache.get(startStation, dailyType, direction)?.let {
//            return it
//        }

        return subwayInfoClient.getTimeTable(startStation, dailyType, direction)
    }
}
