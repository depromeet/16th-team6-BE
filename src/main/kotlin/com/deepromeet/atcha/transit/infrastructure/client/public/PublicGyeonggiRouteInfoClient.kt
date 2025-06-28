package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusRealTimeArrival
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteInfo
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient.Companion.NON_STOP_STATION_NAME
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusRouteStationList
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.DailyTypeResolver
import com.deepromeet.atcha.transit.domain.TransitType
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

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
    override fun getBusRoute(routeName: String): List<BusRoute> {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiRouteInfoFeignClient.getRouteList(key, routeName) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                val routes = (
                    response.msgBody?.busRouteList
                        ?.filter { it.routeName == routeName }
                        ?.map { it.toBusRoute() }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_ROUTE,
                            "경기도 버스 노선 '$routeName'의 해당하는 노선들을 찾을 수 없습니다."
                        )
                )
                if (routes.isEmpty()) {
                    throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_ROUTE,
                        "경기도 버스 노선 '$routeName'의 해당하는 노선들을 찾을 수 없습니다."
                    )
                }
                routes
            },
            errorMessage = "경기도 버스 노선 정보를 가져오는데 실패했습니다. $routeName"
        )
    }

    override fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiRouteInfoFeignClient.getRouteInfo(key, routeInfo.routeId) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                val dailyType = dailyTypeResolver.resolve(TransitType.BUS)
                response.msgBody?.busRouteInfoItem?.toBusSchedule(dailyType, routeInfo.getTargetStation())
                    ?: throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_SCHEDULE,
                        "경기도 버스 노선 '${routeInfo.route.name}' 정류소" +
                            " '${routeInfo.getTargetStation().stationName}'의 도착 정보를 찾을 수 없습니다."
                    )
            },
            errorMessage = "경기도 버스 노선 도착 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiRouteInfoFeignClient.getRouteInfo(key, route.id.value) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody?.busRouteInfoItem?.toBusRouteOperationInfo() ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_ROUTE,
                    "경기도 노선 정보 응답값이 null 입니다."
                )
            },
            errorMessage = "경기도 노선 운행 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getStationList(route: BusRoute): BusRouteStationList {
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

    override fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrival {
        return ApiClientUtils.callApiWithRetry(
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
                response.msgBody?.busRealTimeInfoItem?.toRealTimeArrival() ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_REAL_TIME,
                    "경기도 버스 도착 정보 응답값이 null 입니다."
                )
            },
            errorMessage = "경기도 버스 도착 정보를 가져오는데 실패했습니다."
        )
    }
}
