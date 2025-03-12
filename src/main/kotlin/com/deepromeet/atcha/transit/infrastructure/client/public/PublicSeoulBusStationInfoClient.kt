package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.BusStationMeta
import org.springframework.stereotype.Component

@Component
class PublicSeoulBusStationInfoClient(
    private val publicBusClient: PublicSeoulBusStationInfoFeignClient
) : BusStationInfoClient {
    override fun getStationByName(info: BusStationMeta): BusStation? {
        val response = publicBusClient.getStationInfoByName(info.name)
        val busStations = response.msgBody.itemList?.map { it.toBusStation() }
        return busStations?.minByOrNull { it.busStationMeta.coordinate.distanceTo(info.coordinate) }
    }

    override fun getRoute(
        station: BusStation,
        routeName: String
    ): BusRoute? {
        val busRoutes =
            publicBusClient
                .getRouteByStation(station.id.value)
                .msgBody
                .itemList
                ?.map { it.toBusRoute() }

        return busRoutes?.find { it.name == routeName }
            ?: busRoutes?.find { it.name.contains(routeName) }
    }
}
