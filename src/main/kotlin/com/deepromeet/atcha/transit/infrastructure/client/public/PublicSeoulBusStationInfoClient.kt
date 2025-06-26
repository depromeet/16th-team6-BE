package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteStationList
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
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
    override fun getStationByName(info: BusStationMeta): BusStation {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicBusClient.getStationInfoByName(info.resolveName(), key) },
            isLimitExceeded = { response -> isSeoulApiLimitExceeded(response) },
            processResult = { response ->
                val busStations =
                    response.msgBody.itemList?.map { it.toBusStation() } ?: throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_STATION,
                        "서울시 버스 정류소 '${info.resolveName()}' 응답값이 NULL 입니다."
                    )

                busStations.minByOrNull { it.busStationMeta.coordinate.distanceTo(info.coordinate) }
                    ?: throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_STATION,
                        "서울시 버스 정류소 '${info.resolveName()}'에서 가장 가까운 정류소를 찾을 수 없습니다."
                    )
            },
            errorMessage = "서울시 버스 정류소 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getRoute(
        station: BusStation,
        routeName: String
    ): BusRoute {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicBusClient.getRouteByStation(station.busStationNumber.value, key) },
            isLimitExceeded = { response -> isSeoulApiLimitExceeded(response) },
            processResult = { response ->
                val busRoutes = response.msgBody.itemList?.map { it.toBusRoute() }
                busRoutes?.find { it.name == routeName }
                    ?: busRoutes?.find { it.name.contains(routeName) } ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_ROUTE,
                    "서울시 버스 정류소 '${station.busStationMeta.name}'에서 '$routeName' 노선을 찾을 수 없습니다."
                )
            },
            errorMessage = "서울시 버스 노선 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getByRoute(route: BusRoute): BusRouteStationList {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicBusRouteClient.getStationsByRoute(route.id.value, key) },
            isLimitExceeded = { response -> isSeoulApiLimitExceeded(response) },
            processResult = { response ->
                val responses = response.msgBody.itemList
                if (responses == null) {
                    throw TransitException.of(
                        TransitError.BUS_ROUTE_STATION_LIST_FETCH_FAILED,
                        "버스 노선 '${route.id.value}'의 경유 정류소 정보를 찾을 수 없습니다."
                    )
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
