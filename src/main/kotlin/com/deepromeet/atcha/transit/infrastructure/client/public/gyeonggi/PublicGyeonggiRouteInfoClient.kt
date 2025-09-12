package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi

import com.deepromeet.atcha.transit.application.DailyTypeResolver
import com.deepromeet.atcha.transit.application.bus.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.TransitType
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrivals
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.bus.BusRouteStationList
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class PublicGyeonggiRouteInfoClient(
    private val publicGyeonggiRouteInfoHttpClient: PublicGyeonggiRouteInfoHttpClient,
    private val publicGyeonggiBusRealTimeInfoHttpClient: PublicGyeonggiBusRealTimeInfoHttpClient,
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
        ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key ->
                publicGyeonggiRouteInfoHttpClient.getRouteList(key, routeName)
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

    override suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule {
        val dailyType = dailyTypeResolver.resolve(TransitType.BUS)
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiRouteInfoHttpClient.getRouteInfo(key, routeInfo.routeId) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody?.busRouteInfoItem?.toBusSchedule(dailyType, routeInfo)
                    ?: throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_SCHEDULE,
                        "경기도 버스 노선 '${routeInfo.route.name}' 정류소" +
                            " '${routeInfo.targetStation.stationName}'의 시간표 정보를 찾을 수 없습니다."
                    )
            },
            errorMessage = "경기도 버스 노선 도착 정보를 가져오는데 실패했습니다."
        )
    }

    override suspend fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo =
        ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiRouteInfoHttpClient.getRouteInfo(key, route.id.value) },
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

    @Cacheable(
        cacheNames = ["api:gyeonggi:busRouteStationList"],
        key = "#route.id",
        sync = true,
        cacheManager = "apiCacheManager"
    )
    override suspend fun getStationList(route: BusRoute): BusRouteStationList =
        ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiRouteInfoHttpClient.getRouteStationList(key, route.id.value) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { resp ->
                resp.msgBody?.busRouteStationList
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { routeStations ->
                        val busRouteStations =
                            routeStations.map { it.toBusRouteStation(route) }
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

    override suspend fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrivals =
        ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key ->
                publicGyeonggiBusRealTimeInfoHttpClient.getRealTimeInfo(
                    key,
                    routeInfo.targetStation.stationId,
                    routeInfo.routeId,
                    routeInfo.targetStation.order.toString()
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
                    "정류장(${routeInfo.targetStation.stationId}) 도착 정보를 가져오는데 실패했습니다."
        )
}
