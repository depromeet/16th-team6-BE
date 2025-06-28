package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.location.domain.CoordinateTransformer
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
class PublicIncheonBusStationInfoClient(
    private val incheonBusStationInfoFeignClient: PublicIncheonBusStationInfoFeignClient,
    private val incheonBusRouteInfoFeignClient: PublicIncheonBusRouteInfoFeignClient,
    private val coordinateTransformer: CoordinateTransformer,
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
            apiCall = { key -> incheonBusStationInfoFeignClient.getBusStationByName(key, info.name) },
            isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
            processResult = { response ->
                val busStations =
                    response.msgBody.itemList?.map {
                        val transformToWGS84 = coordinateTransformer.transformToWGS84(it.positionX, it.positionY)
                        it.toBusStation(transformToWGS84)
                    } ?: throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_STATION,
                        "인천시 버스 정류소 '${info.resolveName()}' 응답값이 NULL 입니다."
                    )

                busStations.minByOrNull {
                    info.coordinate.distanceTo(it.busStationMeta.coordinate)
                } ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_STATION,
                    "인천시 버스 정류소 '${info.resolveName()}'에서 가장 가까운 정류소를 찾을 수 없습니다."
                )
            },
            errorMessage = "인천시 버스 정류소 정보를 가져오는데 실패했습니다."
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
            apiCall = { key -> incheonBusStationInfoFeignClient.getBusRoutesByStation(key, station.id.value) },
            isLimitExceeded = { response -> ApiClientUtils.isServiceResultApiLimitExceeded(response) },
            processResult = { response ->
                val routeResponse = (
                    response.msgBody.itemList?.find { it.routeNumber == routeName }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_ROUTE,
                            "인천시'${station.id}'정류장에서 '$routeName'노선을 찾을 수 없습니다."
                        )
                )
                routeResponse.toBusRoute()
            },
            errorMessage = "인천시 버스 노선 정보를 가져오는데 실패했습니다."
        )
    }

    override fun getByRoute(route: BusRoute): BusRouteStationList {
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
}
