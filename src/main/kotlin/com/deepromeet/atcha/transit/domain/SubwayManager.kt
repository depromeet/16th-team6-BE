package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import org.springframework.stereotype.Component

@Component
class SubwayManager(
    private val subwayStationRepository: SubwayStationRepository,
    private val subwayInfoClient: SubwayInfoClient,
    private val dailyTypeResolver: DailyTypeResolver
) {
    fun getStation(stationMeta: SubwayStationMeta): SubwayStation {
        return subwayStationRepository.findByRouteNameAndNameContaining(stationMeta.name, stationMeta.routeName)
            ?: throw TransitException.NotFoundSubwayStation
    }

    fun getLastTime(
        startStation: SubwayStation,
        endStation: SubwayStation
    ): SubwayTime? {
        val timeTable =
            subwayInfoClient.getTimeTable(
                startStation,
                dailyTypeResolver.resolve(),
                SubwayDirectionResolver.resolve(startStation, endStation)
            )

        if (timeTable.schedule.isEmpty()) {
            throw TransitException.NotFoundSubwayTimeTable
        }

        return timeTable.getLastTime(endStation)
    }
}
