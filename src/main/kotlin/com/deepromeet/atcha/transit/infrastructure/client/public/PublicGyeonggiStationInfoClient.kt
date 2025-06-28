package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteStationList
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.BusStationInfoClient.Companion.NON_STOP_STATION_NAME
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
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
    override fun getStationByName(info: BusStationMeta): BusStation {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiBusStationInfoFeignClient.getStationList(key, info.resolveName()) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                val busStations =
                    response.msgBody?.busStationList?.map { it.toBusStation() }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_STATION,
                            "경기도 버스 정류소-${info.resolveName()} 응닶값이 NULL 입니다."
                        )
                busStations.minByOrNull { it.busStationMeta.coordinate.distanceTo(info.coordinate) }
                    ?: throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_STATION,
                        "경기도 버스 정류소-${info.resolveName()}에서 가장 가까운 정류소를 찾을 수 없습니다."
                    )
            },
            errorMessage = "경기도 버스 정류소-${info.resolveName()} 정보를 가져오는데 실패했습니다."
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
            apiCall = { key -> publicGyeonggiBusStationInfoFeignClient.getStationRouteList(key, station.id.value) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody?.busRouteList
                    ?.firstOrNull { it.routeName.trim() == routeName.trim() }
                    ?.toBusRoute() ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_ROUTE,
                    "경기도 버스 노선 정보 - $routeName-${station.busStationNumber.value}를 찾을 수 없습니다."
                )
            },
            errorMessage = "경기도 버스 노선 정보 - $routeName-${station}를 가져오는데 실패했습니다."
        )
    }

    override fun getByRoute(route: BusRoute): BusRouteStationList {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiRouteInfoFeignClient.getRouteStationList(key, route.id.value) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                val busRouteStationsResponse =
                    response.msgBody?.busRouteStationList
                        ?: throw TransitException.of(
                            TransitError.BUS_ROUTE_STATION_LIST_FETCH_FAILED,
                            "경기도 버스 노선 '${route.name}-${route.id.value}'의 경유 정류소를 찾을 수 없습니다."
                        )

                BusRouteStationList(
                    busRouteStationsResponse
                        .filter { station ->
                            NON_STOP_STATION_NAME.none { keyword -> station.stationName.contains(keyword) }
                        }
                        .map { it.toBusRouteStation(route) },
                    busRouteStationsResponse.firstOrNull()?.turnSeq
                )
            },
            errorMessage = "경기도 버스 노선 경유 정류소를 가져오는데 실패했습니다."
        )
    }
}
