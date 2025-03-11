package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusArrivalInfoFetcher
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.DailyTypeResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

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
    ): BusArrival {
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
    }
}
