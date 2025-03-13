package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.BusStationMeta
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicSeoulBusStationInfoClient(
    private val publicBusClient: PublicSeoulBusStationInfoFeignClient
) : BusStationInfoClient {
    override fun getStationByName(info: BusStationMeta): BusStation? {
        try {
            val response = publicBusClient.getStationInfoByName(info.name)
            val busStations = response.msgBody.itemList?.map { it.toBusStation() }
            return busStations?.minByOrNull { it.busStationMeta.coordinate.distanceTo(info.coordinate) }
        } catch (e: Exception) {
            log.warn(e) { "서울시 버스 정류소 정보를 가져오는데 실패했습니다." }
            return null
        }
    }

    override fun getRoute(
        station: BusStation,
        routeName: String
    ): BusRoute? {
        try {
            val busRoutes =
                publicBusClient
                    .getRouteByStation(station.id.value)
                    .msgBody
                    .itemList
                    ?.map { it.toBusRoute() }

            return busRoutes?.find { it.name == routeName }
                ?: busRoutes?.find { it.name.contains(routeName) }
        } catch (e: Exception) {
            log.warn(e) { "서울시 버스 노선 정보를 가져오는데 실패했습니다." }
            return null
        }
    }
}
