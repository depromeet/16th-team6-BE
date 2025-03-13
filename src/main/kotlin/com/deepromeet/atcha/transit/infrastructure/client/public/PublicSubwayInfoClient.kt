package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.SubwayDirection
import com.deepromeet.atcha.transit.domain.SubwayInfoClient
import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationData
import com.deepromeet.atcha.transit.domain.SubwayTimeTable
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

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
            }
            ?.toData()
    }

    override fun getTimeTable(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable? {
        try {
            val subwayStations = subwayStationRepository.findByRouteCode(startStation.routeCode)
            val items =
                subwayInfoFeignClient
                    .getStationSchedule(serviceKey, startStation.id.value, dailyType.code, direction.code)
                    .response.body.items?.item
                    ?.filter { it.endSubwayStationNm != null }
                    ?.parallelStream()
                    ?.map {
                        val finalStation =
                            subwayStations.find { station -> station.name == it.endSubwayStationNm }
                                ?: throw TransitException.NotFoundSubwayStation
                        it.toDomain(finalStation)
                    }
                    ?.toList()
                    ?: return null

            return SubwayTimeTable(
                startStation,
                dailyType,
                direction,
                items
            )
        } catch (e: Exception) {
            log.warn(e) { "지하철 시간표를 가져오는데 실패했습니다." }
            return null
        }
    }
}
