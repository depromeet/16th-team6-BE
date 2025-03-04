package com.deepromeet.atcha.transit.infrastructure.client.openapi

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.StationInfo
import org.springframework.stereotype.Component

@Component
class PublicBusStationInfoClient(
    private val publicBusClient: PublicBusStationInfoFeignClient
) : BusStationInfoClient {
    override fun getStationByName(info: StationInfo): BusStation? {
        val response = publicBusClient.getStationInfoByName(info.name)
        val busStations = response.msgBody.itemList?.map { it.toBusStation() }
        return busStations?.minByOrNull { it.stationInfo.coordinate.distanceTo(info.coordinate) }
    }

    override fun getRoute(
        station: BusStation,
        routeName: String
    ): BusRoute? {
        val busRoutes =
            publicBusClient
                .getRouteByStation(station.arsId.value)
                .msgBody
                .itemList
                ?.map { it.toBusRoute() }

        return busRoutes?.find { it.name == routeName }
            ?: busRoutes?.find { it.name.contains(routeName) }
    }
}
