package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.BusStationMeta
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicGyeonggiStationInfoClient(
    private val publicGyeonggiBusStationInfoFeignClient: PublicGyeonggiBusStationInfoFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) : BusStationInfoClient {
    override fun getStationByName(info: BusStationMeta): BusStation? {
        val stationList = publicGyeonggiBusStationInfoFeignClient.getStationList(serviceKey, info.name)
        val busStations = stationList.response.msgBody.busStationList.map { it.toBusStation() }
        return busStations.minByOrNull { it.busStationMeta.coordinate.distanceTo(info.coordinate) }
    }

    override fun getRoute(
        station: BusStation,
        routeName: String
    ): BusRoute? {
        val stationRouteList = publicGyeonggiBusStationInfoFeignClient.getStationRouteList(serviceKey, station.id.value)
        return stationRouteList.response.msgBody.busRouteList.firstOrNull { it.routeName == routeName }?.toBusRoute()
    }
}
