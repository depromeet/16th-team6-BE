package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.SubwayDirection
import com.deepromeet.atcha.transit.domain.SubwayInfoClient
import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationData
import com.deepromeet.atcha.transit.domain.SubwayTimeTable
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.response.SubwayTimeResponse
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
            ?.find { it.subwayRouteName == routeName } // TODO : KRIC 노선이름과 공공데이터의 노선이름이 일치하는지 확인 필요
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
                ?.map {
                    val finalStation = findSubwayStation(startStation, it)
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
        subwayTimeResponse: SubwayTimeResponse
    ): SubwayStation {
        val finalStation =
            subwayStationRepository.findByRouteNameAndNameContaining(
                startStation.routeName,
                subwayTimeResponse.endSubwayStationNm
            ) ?: throw TransitException.NotFoundSubwayStation
        return finalStation
    }
}
