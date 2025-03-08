package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.SubwayDirection
import com.deepromeet.atcha.transit.domain.SubwayInfoClient
import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationData
import com.deepromeet.atcha.transit.domain.SubwayTimeTable
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicSubwayInfoClient(
    private val subwayInfoFeignClient: PublicSubwayInfoFeignClient,
    private val subwayStationRepository: SubwayStationRepository,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) : SubwayInfoClient {
    fun getSubwayStationByName(
        stationName: String,
        routeName: String
    ): SubwayStationData? {
        return subwayInfoFeignClient.getStationByName(serviceKey, stationName)
            .response
            .body
            .items
            ?.item
            ?.find {
                it.subwayRouteName == routeName && it.subwayStationName == stationName
            } // TODO : KRIC 노선이름과 공공데이터의 노선이름이 일치하는지 확인 필요
            ?.toData()
    }

    override fun getTimeTable(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable {
        val items =
            subwayInfoFeignClient.getStationSchedule(serviceKey, startStation.id.value, dailyType.code, direction.code)
                .response.body.items?.item
                ?.filter { it.endSubwayStationNm != null }
                ?.map {
                    val finalStation = findSubwayStation(startStation, it.endSubwayStationNm!!)
                    it.toDomain(finalStation)
                }
                ?: emptyList()

        return SubwayTimeTable(
            startStation,
            dailyType,
            direction,
            items
        )
    }

    private fun findSubwayStation(
        startStation: SubwayStation,
        endStationName: String
    ): SubwayStation {
        val finalStation =
            subwayStationRepository.findByRouteCodeAndName(
                startStation.routeCode,
                endStationName
            ) ?: throw TransitException.NotFoundSubwayStation
        return finalStation
    }
}
