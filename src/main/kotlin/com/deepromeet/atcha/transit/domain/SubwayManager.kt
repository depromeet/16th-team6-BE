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
    private val subwayBranchRepository: SubwayBranchRepository
) {
    fun getLastTime(
        subwayLine: SubwayLine,
        startStationName: String,
        endStationName: String
    ): SubwayTime? {
        val routes = getRoutes(subwayLine.lnCd)
        val startStation = getStation(subwayLine.lnCd, startStationName)
        val endStation = getStation(subwayLine.lnCd, endStationName)

        val timeTable =
            subwayInfoClient.getTimeTable(
                startStation,
                dailyTypeResolver.resolve(),
                SubwayDirection.resolve(routes, startStation, endStation)
            )

        if (timeTable.schedule.isEmpty()) {
            throw TransitException.NotFoundSubwayTimeTable
        }

        return timeTable.getLastTime(endStation, routes)
    }

    private fun getStation(
        routeCode: String,
        stationName: String
    ): SubwayStation {
        return subwayStationRepository.findByRouteCodeAndName(routeCode, stationName)
            ?: throw TransitException.NotFoundSubwayStation
    }

    private fun getRoutes(routeCode: String) =
        subwayBranchRepository.findByRouteCode(routeCode)
            .groupBy { it.finalStationName }
            .values
            .map { Route(it) }
}
