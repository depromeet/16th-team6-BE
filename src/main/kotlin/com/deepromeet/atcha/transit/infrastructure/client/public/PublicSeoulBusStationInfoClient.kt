package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteStationList
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.infrastructure.client.public.ApiClientUtils.isSeoulApiLimitExceeded
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicSeoulBusStationInfoClient(
    private val publicBusClient: PublicSeoulBusStationInfoFeignClient,
    private val publicBusRouteClient: PublicSeoulBusRouteInfoFeignClient,
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
            apiCall = { key -> publicBusClient.getStationInfoByName(info.resolveName(), key) },
            isLimitExceeded = { response -> isSeoulApiLimitExceeded(response) },
            processResult = { response ->
                val busStations = response.msgBody.itemList?.map { it.toBusStation() }
                busStations?.minByOrNull { it.busStationMeta.coordinate.distanceTo(info.coordinate) }
            },
            errorMessage = "서울시 버스 정류소 정보를 가져오는데 실패했습니다."
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
            apiCall = { key -> publicBusClient.getRouteByStation(station.busStationNumber.value, key) },
            isLimitExceeded = { response -> isSeoulApiLimitExceeded(response) },
            processResult = { response ->
                val busRoutes = response.msgBody.itemList?.map { it.toBusRoute() }
                busRoutes?.find { it.name == routeName }
                    ?: busRoutes?.find { it.name.contains(routeName) }
            },
            errorMessage = "서울시 버스 노선 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getByRoute(route: BusRoute): BusRouteStationList? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicBusRouteClient.getStationsByRoute(route.id.value, key) },
            isLimitExceeded = { response -> isSeoulApiLimitExceeded(response) },
            processResult = { response ->
                val responses = response.msgBody.itemList
                if (responses == null) {
                    null
                } else {
                    val busRouteStations = responses.map { it.toBusRouteStation() }
                    val turnPoint = responses.first { it.transYn == "Y" }.seq.toInt()

                    BusRouteStationList(busRouteStations, turnPoint)
                }
            },
            errorMessage = "서울시 버스 노선 경유 정류소를 가져오는데 실패했습니다."
        )
    }
}
