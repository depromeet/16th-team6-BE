package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteStationList
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.BusStationMeta
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicGyeonggiStationInfoClient(
    private val publicGyeonggiBusStationInfoFeignClient: PublicGyeonggiBusStationInfoFeignClient,
    private val publicGyeonggiRouteInfoFeignClient: PublicGyeonggiRouteInfoFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) : BusStationInfoClient {
    override fun getStationByName(info: BusStationMeta): BusStation? {
        try {
            val stationList = publicGyeonggiBusStationInfoFeignClient.getStationList(serviceKey, info.name)
            val busStations = stationList.response.msgBody.busStationList.map { it.toBusStation() }
            return busStations.minByOrNull { it.busStationMeta.coordinate.distanceTo(info.coordinate) }
        } catch (e: Exception) {
            log.warn(e) { "경기도 버스 정류소 정보를 가져오는데 실패했습니다." }
            return null
        }
    }

    override fun getRoute(
        station: BusStation,
        routeName: String
    ): BusRoute? {
        try {
            val stationRouteList =
                publicGyeonggiBusStationInfoFeignClient.getStationRouteList(
                    serviceKey,
                    station.id.value
                )
            return stationRouteList.response
                .msgBody
                .busRouteList
                .firstOrNull { it.routeName == routeName }
                ?.toBusRoute()
        } catch (e: Exception) {
            log.warn(e) { "경기도 버스 노선 정보를 가져오는데 실패했습니다." }
            return null
        }
    }

    override fun getByRoute(route: BusRoute): BusRouteStationList? {
        return try {
            val busRouteStationsResponse =
                publicGyeonggiRouteInfoFeignClient.getRouteStationList(
                    serviceKey,
                    route.id.value
                ).response.msgBody.busRouteStationList

            BusRouteStationList(
                busRouteStationsResponse.map { it.toBusRouteStation(route) },
                busRouteStationsResponse.firstOrNull()?.turnSeq
            )
        } catch (e: Exception) {
            log.warn(e) { "경기도 버스 노선 경유 정류소를 가져오는데 실패했습니다." }
            null
        }
    }
}
