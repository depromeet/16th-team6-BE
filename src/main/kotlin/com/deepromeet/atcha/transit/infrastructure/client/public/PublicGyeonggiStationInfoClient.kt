package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteStationList
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.BusStationMeta
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicGyeonggiStationInfoClient(
    private val publicGyeonggiBusStationInfoFeignClient: PublicGyeonggiBusStationInfoFeignClient,
    private val publicGyeonggiRouteInfoFeignClient: PublicGyeonggiRouteInfoFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : BusStationInfoClient {
    override fun getStationByName(info: BusStationMeta): BusStation? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiBusStationInfoFeignClient.getStationList(key, info.name) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                val busStations = response.response.msgBody.busStationList.map { it.toBusStation() }
                busStations.minByOrNull { it.busStationMeta.coordinate.distanceTo(info.coordinate) }
            },
            errorMessage = "경기도 버스 정류소 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getRoute(
        station: BusStation,
        routeName: String
    ): BusRoute? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiBusStationInfoFeignClient.getStationRouteList(key, station.id.value) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                response.response.msgBody.busRouteList
                    .firstOrNull { it.routeName == routeName }
                    ?.toBusRoute()
            },
            errorMessage = "경기도 버스 노선 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getByRoute(route: BusRoute): BusRouteStationList? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiRouteInfoFeignClient.getRouteStationList(key, route.id.value) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                val busRouteStationsResponse = response.response.msgBody.busRouteStationList

                BusRouteStationList(
                    busRouteStationsResponse.map { it.toBusRouteStation(route) },
                    busRouteStationsResponse.firstOrNull()?.turnSeq
                )
            },
            errorMessage = "경기도 버스 노선 경유 정류소를 가져오는데 실패했습니다."
        )
    }
}
