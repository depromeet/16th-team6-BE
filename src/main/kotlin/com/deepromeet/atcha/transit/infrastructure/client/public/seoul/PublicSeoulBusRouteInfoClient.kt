package com.deepromeet.atcha.transit.infrastructure.client.public.seoul

import com.deepromeet.atcha.transit.domain.BusRealTimeArrival
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteInfo
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient.Companion.NON_STOP_STATION_NAME
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusRouteStationList
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils.isServiceResultApiLimitExceeded
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import kotlin.collections.filter
import kotlin.collections.first

@Component
class PublicSeoulBusRouteInfoClient(
    private val publicSeoulBusArrivalInfoFeignClient: PublicSeoulBusArrivalInfoFeignClient,
    private val publicBusRouteClient: PublicSeoulBusRouteInfoFeignClient,
    private val seoulBusOperationFeignClient: SeoulBusOperationFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : BusRouteInfoClient {
    @Cacheable(
        cacheNames = ["api:seoul:busRouteList"],
        key = "#routeName",
        sync = true
    )
    override suspend fun getBusRoute(routeName: String): List<BusRoute> {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicSeoulBusArrivalInfoFeignClient.getBusRouteList(key, routeName) },
            isLimitExceeded = { response -> isServiceResultApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody.itemList
                    ?.filter { it.busRouteName == routeName || it.busRouteAbrv == routeName }
                    ?.map { it.toBusRoute() }
                    ?.takeIf { it.isNotEmpty() }
                    ?: throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_ROUTE,
                        "서울시 버스 노선 '$routeName'의 정보를 찾을 수 없습니다."
                    )
            },
            errorMessage = "서울시 버스 노선 정보를 가져오는데 실패했습니다."
        )
    }

    override suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key ->
                publicSeoulBusArrivalInfoFeignClient
                    .getArrivalInfoByRoute(
                        key,
                        routeInfo.route.id.value,
                        routeInfo.getTargetStation().stationId,
                        routeInfo.getTargetStation().order
                    )
            },
            isLimitExceeded = { response -> isServiceResultApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody.itemList
                    ?.getOrNull(0)
                    ?.toBusSchedule(routeInfo.getTargetStation().busStation)
                    ?: throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_REAL_TIME,
                        "서울시 버스(${routeInfo.routeId}의" +
                            " 정류소(${routeInfo.getTargetStation().stationId}" +
                            "-${routeInfo.getTargetStation().order})의 실시간 도착 정보를 찾을 수 없습니다."
                    )
            },
            errorMessage = "서울시 버스 도착 정보를 가져오는데 실패했습니다."
        )
    }

    override suspend fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo {
        return seoulBusOperationFeignClient.getBusOperationInfo(route.id.value).toBusRouteOperationInfo()
            ?: throw TransitException.of(
                TransitError.NOT_FOUND_BUS_OPERATION_INFO,
                "서울시 버스 노선 '${route.id.value}'의 운영 정보를 찾을 수 없습니다."
            )
    }

    @Cacheable(
        cacheNames = ["api:seoul:busRouteStationList"],
        key = "#route.id",
        sync = true,
        cacheManager = "apiCacheManager"
    )
    override suspend fun getStationList(route: BusRoute): BusRouteStationList {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicBusRouteClient.getStationsByRoute(route.id.value, key) },
            isLimitExceeded = { response -> isServiceResultApiLimitExceeded(response) },
            processResult = { response ->
                val responses = response.msgBody.itemList
                if (responses == null) {
                    throw TransitException.of(
                        TransitError.BUS_ROUTE_STATION_LIST_FETCH_FAILED,
                        "버스 노선 '${route.id.value}'의 경유 정류소 정보를 찾을 수 없습니다."
                    )
                } else {
                    val turnPoint = responses.first { it.transYn == "Y" }.seq.toInt()
                    val busRouteStations =
                        responses
                            .filter { station ->
                                NON_STOP_STATION_NAME.none { keyword -> station.stationNm.contains(keyword) }
                            }
                            .map { it.toBusRouteStation(turnPoint) }
                    BusRouteStationList(busRouteStations, turnPoint)
                }
            },
            errorMessage = "서울시 버스 노선 경유 정류소를 가져오는데 실패했습니다."
        )
    }

    override suspend fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrival {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = {
                    key ->
                publicSeoulBusArrivalInfoFeignClient
                    .getArrivalInfoByRoute(
                        key,
                        routeInfo.route.id.value,
                        routeInfo.getTargetStation().stationId,
                        routeInfo.getTargetStation().order
                    )
            },
            isLimitExceeded = { response -> isServiceResultApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody.itemList
                    ?.getOrNull(0)
                    ?.toBusRealTimeArrival()
                    ?: throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_REAL_TIME,
                        "서울시 버스(${routeInfo.routeId}의" +
                            " 정류소(${routeInfo.getTargetStation().stationId}" +
                            "-${routeInfo.getTargetStation().order})의 실시간 도착 정보를 찾을 수 없습니다."
                    )
            },
            errorMessage = "서울시 버스 실시간 정보를 가져오는데 실패했습니다."
        )
    }
}
