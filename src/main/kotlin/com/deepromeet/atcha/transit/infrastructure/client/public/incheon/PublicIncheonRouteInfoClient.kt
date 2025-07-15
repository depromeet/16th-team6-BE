package com.deepromeet.atcha.transit.infrastructure.client.public.incheon

import com.deepromeet.atcha.location.domain.CoordinateTransformer
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrival
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfoClient.Companion.NON_STOP_STATION_NAME
import com.deepromeet.atcha.transit.domain.bus.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.bus.BusRouteStationList
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class PublicIncheonRouteInfoClient(
    private val publicIncheonBusArrivalFeignClient: PublicIncheonBusArrivalFeignClient,
    private val incheonBusRouteInfoFeignClient: PublicIncheonBusRouteInfoFeignClient,
    private val coordinateTransformer: CoordinateTransformer,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : BusRouteInfoClient {
    @Cacheable(
        cacheNames = ["api:incheon:busRouteList"],
        key = "#routeName",
        sync = true,
        cacheManager = "apiCacheManager"
    )
    override suspend fun getBusRoute(routeName: String): List<BusRoute> =
        withContext(Dispatchers.IO) {
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key -> incheonBusRouteInfoFeignClient.getBusRouteByName(key, routeName) },
                isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
                processResult = { response ->
                    response.msgBody.itemList
                        ?.filter { it.routeNumber == routeName }
                        ?.map { it.toBusRoute() }
                        ?.takeIf { it.isNotEmpty() }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_ROUTE,
                            "인천시 버스 노선 '$routeName'의 정보를 찾을 수 없습니다."
                        )
                },
                errorMessage = "인천시 버스 노선 정보를 가져오는데 실패했습니다."
            )
        }

    override suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule =
        withContext(Dispatchers.IO) {
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key ->
                    incheonBusRouteInfoFeignClient.getBusRouteInfoById(key, routeInfo.routeId)
                },
                isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
                processResult = { response ->
                    response.msgBody.itemList?.get(0)?.toBusSchedule(routeInfo.getTargetStation())
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_ROUTE,
                            "인천시 버스 노선-${routeInfo.route.name}-${routeInfo.route.id.value}의 정보를 찾을 수 없습니다."
                        )
                },
                errorMessage = "인천시 버스 정류소-${routeInfo.getTargetStation().stationId} 정보를 가져오는데 실패했습니다."
            )
        }

    override suspend fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo =
        withContext(Dispatchers.IO) {
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key -> incheonBusRouteInfoFeignClient.getBusRouteInfoById(key, route.id.value) },
                isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
                processResult = { response ->
                    response.msgBody.itemList?.get(0)?.toBusRouteOperationInfo()
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_OPERATION_INFO,
                            "인천시 버스 노선-${route.name}-${route.id.value}의 운영 정보를 찾을 수 없습니다."
                        )
                },
                errorMessage = "인천시 버스 노선-${route.name}-${route.id.value}의 운영 정보를 가져오는데 실패했습니다."
            )
        }

    @Cacheable(
        cacheNames = ["api:incheon:busRouteStationList"],
        key = "#route.id",
        sync = true,
        cacheManager = "apiCacheManager"
    )
    override suspend fun getStationList(route: BusRoute): BusRouteStationList =
        withContext(Dispatchers.IO) {
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key ->
                    incheonBusRouteInfoFeignClient.getBusRouteSectionList(key, route.id.value)
                },
                isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
                processResult = { response ->
                    val turnPoint = response.msgBody.itemList?.first { it.directionCode == 1 }

                    val routeStations =
                        response.msgBody.itemList
                            ?.filter { station ->
                                NON_STOP_STATION_NAME.none { keyword -> station.stationName.contains(keyword) }
                            }
                            ?.map {
                                it.toBusRouteStation(
                                    route,
                                    turnPoint?.stationSequence,
                                    coordinateTransformer.transformToWGS84(it.positionX, it.positionY)
                                )
                            }
                            ?: throw TransitException.of(
                                TransitError.BUS_ROUTE_STATION_LIST_FETCH_FAILED,
                                "인천시 버스 노선-${route.name}-${route.id.value}의 경유 정류소 정보를 찾을 수 없습니다."
                            )

                    BusRouteStationList(
                        routeStations,
                        turnPoint?.stationSequence
                    )
                },
                errorMessage = "인천시 버스 노선 스케줄 정보를 가져오는데 실패했습니다."
            )
        }

    override suspend fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrival =
        withContext(Dispatchers.IO) {
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key ->
                    publicIncheonBusArrivalFeignClient.getBusArrivalList(
                        key,
                        routeInfo.routeId,
                        routeInfo.getTargetStation().stationId
                    )
                },
                isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
                processResult = { response ->
                    response.msgBody.itemList?.getOrNull(0)
                        ?.toBusRealTimeArrival()
                        ?: BusRealTimeArrival(emptyList())
                },
                errorMessage =
                    "인천시 버스(${routeInfo.route.id})의 " +
                        "정류장(${routeInfo.getTargetStation().stationId}) 도착 정보를 가져오는데 실패했습니다."
            )
        }
}
