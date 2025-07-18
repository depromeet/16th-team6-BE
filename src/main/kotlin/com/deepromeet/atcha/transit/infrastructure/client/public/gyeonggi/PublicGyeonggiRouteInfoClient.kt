package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi

import com.deepromeet.atcha.transit.application.DailyTypeResolver
import com.deepromeet.atcha.transit.application.bus.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.TransitType
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrival
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
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
class PublicGyeonggiRouteInfoClient(
    private val publicGyeonggiRouteInfoFeignClient: PublicGyeonggiRouteInfoFeignClient,
    private val publicGyeonggiBusRealTimeInfoFeignClient: PublicGyeonggiBusRealTimeInfoFeignClient,
    private val dailyTypeResolver: DailyTypeResolver,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : BusRouteInfoClient {
    @Cacheable(
        cacheNames = ["api:gyeonggi:busRouteList"],
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
                apiCall = { key ->
                    publicGyeonggiRouteInfoFeignClient.getRouteList(key, routeName)
                },
                isLimitExceeded = ApiClientUtils::isGyeonggiApiLimitExceeded,
                processResult = { resp ->
                    resp.msgBody?.busRouteList
                        ?.filter { it.routeName == routeName }
                        ?.map { it.toBusRoute() }
                        ?.takeIf { it.isNotEmpty() }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_ROUTE,
                            "경기도 버스 노선 '$routeName' 정보를 찾을 수 없습니다."
                        )
                },
                errorMessage = "경기도 버스 노선 정보를 가져오는데 실패했습니다. $routeName"
            )
        }

    override suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule =
        withContext(Dispatchers.IO) {
            val dailyType = dailyTypeResolver.resolve(TransitType.BUS)
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key -> publicGyeonggiRouteInfoFeignClient.getRouteInfo(key, routeInfo.routeId) },
                isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
                processResult = { response ->
                    response.msgBody?.busRouteInfoItem?.toBusSchedule(dailyType, routeInfo.getTargetStation())
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_SCHEDULE,
                            "경기도 버스 노선 '${routeInfo.route.name}' 정류소" +
                                " '${routeInfo.getTargetStation().stationName}'의 시간표 정보를 찾을 수 없습니다."
                        )
                },
                errorMessage = "경기도 버스 노선 도착 정보를 가져오는데 실패했습니다."
            )
        }

    override suspend fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo =
        withContext(Dispatchers.IO) {
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key -> publicGyeonggiRouteInfoFeignClient.getRouteInfo(key, route.id.value) },
                isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
                processResult = { response ->
                    response.msgBody?.busRouteInfoItem?.toBusRouteOperationInfo()
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_OPERATION_INFO,
                            "경기도 $route 노선 정보을 가져올 수 없습니다."
                        )
                },
                errorMessage = "경기도 노선 운행 정보를 가져오는데 실패했습니다."
            )
        }

    @Cacheable(
        cacheNames = ["api:gyeonggi:busRouteStationList"],
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
                apiCall = { key -> publicGyeonggiRouteInfoFeignClient.getRouteStationList(key, route.id.value) },
                isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
                processResult = { resp ->
                    resp.msgBody?.busRouteStationList
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { routeStations ->
                            val busRouteStations =
                                routeStations.map { it.toBusRouteStation(route) }
                                    .filter(::isValidStation)
                            BusRouteStationList(
                                busRouteStations,
                                routeStations.first().turnSeq
                            )
                        }
                        ?: throw TransitException.of(
                            TransitError.BUS_ROUTE_STATION_LIST_FETCH_FAILED,
                            "경기도 버스 노선 '${route.name}-${route.id.value}' 경유 정류소를 찾을 수 없습니다."
                        )
                },
                errorMessage = "경기도 버스 노선 경유 정류소를 가져오는데 실패했습니다."
            )
        }

    override suspend fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrival =
        withContext(Dispatchers.IO) {
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key ->
                    publicGyeonggiBusRealTimeInfoFeignClient.getRealTimeInfo(
                        key,
                        routeInfo.getTargetStation().stationId,
                        routeInfo.routeId,
                        routeInfo.getTargetStation().order.toString()
                    )
                },
                isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
                processResult = { response ->
                    response.msgBody?.busArrivalItem?.toRealTimeArrival()
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_REAL_TIME,
                            "경기도 버스 도착 정보를 가져올 수 없습니다."
                        )
                },
                errorMessage =
                    "경기도 버스(${routeInfo.route.id})의 " +
                        "정류장(${routeInfo.getTargetStation().stationId}) 도착 정보를 가져오는데 실패했습니다."
            )
        }
}
