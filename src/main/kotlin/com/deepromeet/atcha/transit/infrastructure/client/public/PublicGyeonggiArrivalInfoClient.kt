package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusArrivalInfoFetcher
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.DailyTypeResolver
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicGyeonggiArrivalInfoClient(
    private val publicGyeonggiRouteInfoFeignClient: PublicGyeonggiRouteInfoFeignClient,
    private val publicGyeonggiBusArrivalInfoFeignClient: PublicGyeonggiBusArrivalInfoFeignClient,
    private val dailyTypeResolver: DailyTypeResolver,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) : BusArrivalInfoFetcher {
    override fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival? {
        try {
            val busRouteInfo =
                publicGyeonggiRouteInfoFeignClient.getRouteInfo(
                    serviceKey,
                    route.id.value
                ).response.msgBody.busRouteInfoItem
            val busRouteStations =
                publicGyeonggiRouteInfoFeignClient.getRouteStationList(
                    serviceKey,
                    route.id.value
                ).response.msgBody
            val stationInfo = busRouteStations.getStation(station.id)
            val arrivalInfo =
                publicGyeonggiBusArrivalInfoFeignClient.getArrivalInfo(
                    serviceKey,
                    station.id.value,
                    route.id.value,
                    stationInfo.stationSeq.toString()
                ).response.msgBody.busArrivalItem
            return busRouteInfo.toBusArrival(
                dailyTypeResolver.resolve(),
                stationInfo.getDirection(),
                arrivalInfo,
                busRouteStations
            )
        } catch (e: Exception) {
            log.warn(e) { "경기도 버스 도착 정보를 가져오는데 실패했습니다." }
            return null
        }
    }
}
