package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.location.domain.CoordinateTransformer
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
import org.springframework.beans.factory.annotation.Value
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
    override fun getBusRoute(routeName: String): List<BusRoute> {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> incheonBusRouteInfoFeignClient.getBusRouteByName(key, routeName) },
            isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
            processResult = { response ->
                val routes = (
                    response.msgBody.itemList?.filter { it.routeNumber == routeName }?.map { it.toBusRoute() }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_ROUTE,
                            "인천시 버스 노선 '$routeName'의 정보를 찾을 수 없습니다."
                        )
                )
                if (routes.isEmpty()) {
                    throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_ROUTE,
                        "인천시 버스 노선 '$routeName'의 해당하는 노선들을 찾을 수 없습니다."
                    )
                }
                routes
            },
            errorMessage = "인천시 버스 노선 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule? {
        return ApiClientUtils.callApiWithRetry(
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

    override fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo {
        return ApiClientUtils.callApiWithRetry(
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

    override fun getStationList(route: BusRoute): BusRouteStationList {
        return ApiClientUtils.callApiWithRetry(
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
                            TransitError.NOT_FOUND_BUS_STATION,
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

    override fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrival {
        return ApiClientUtils.callApiWithRetry(
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
                response.msgBody.itemList?.get(0)?.toBusRealTimeArrival()
                    ?: BusRealTimeArrival(emptyList())
            },
            errorMessage =
                "인천시 버스 노선 - " +
                    "${routeInfo.getTargetStation().stationId}-${routeInfo.route.id.value}의 도착 정보를 가져오는데 실패했습니다."
        )
    }
}
