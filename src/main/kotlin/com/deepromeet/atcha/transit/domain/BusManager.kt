package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.odsay.ODSayBusInfoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

val log = KotlinLogging.logger {}

@Component
class BusManager(
    private val oDSayBusInfoClient: ODSayBusInfoClient,
    private val busStationInfoClientMap: Map<ServiceRegion, BusStationInfoClient>,
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val busPositionFetcherMap: Map<ServiceRegion, BusPositionFetcher>,
    private val regionIdentifier: RegionIdentifier,
    private val busTimeTableCache: BusTimeTableCache
) {
    fun getSchedule(
        routeName: String,
        meta: BusStationMeta
    ): BusSchedule {
        val (station, route) = findStationAndRoute(routeName, meta)

        val schedule =
            busRouteInfoClientMap[route.serviceRegion]?.getBusSchedule(station, route)
                ?: oDSayBusInfoClient.getBusSchedule(station, route)
                ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_SCHEDULE,
                    "버스 노선 '$routeName' 정류소 '${meta.name}'의 도착 정보를 찾을 수 없습니다."
                )

        busTimeTableCache.cache(routeName, meta, schedule.busTimeTable)
        return schedule
    }

    fun getRealTimeArrival(
        routeName: String,
        meta: BusStationMeta
    ): BusRealTimeArrival {
        val (station, route) = findStationAndRoute(routeName, meta)

        return busRouteInfoClientMap[route.serviceRegion]
            ?.getBusRealTimeInfo(station, route)
            ?: throw TransitException.of(
                TransitError.NOT_FOUND_BUS_REAL_TIME,
                "버스 노선 '$routeName' 정류소 '${meta.name}'의 실시간 정보를 찾을 수 없습니다."
            )
    }

    fun getBusTimeInfo(
        routeName: String,
        stationMeta: BusStationMeta
    ): BusTimeTable {
        return busTimeTableCache.get(
            routeName,
            stationMeta
        ) ?: getSchedule(routeName, stationMeta).busTimeTable
    }

    fun getBusRouteOperationInfo(route: BusRoute): BusRouteOperationInfo {
        return busRouteInfoClientMap[route.serviceRegion]!!.getBusRouteInfo(route)
            ?: throw TransitException.of(
                TransitError.NOT_FOUND_BUS_OPERATION_INFO,
                "버스 노선 '${route.name}'의 운행 정보를 찾을 수 없습니다."
            )
    }

    suspend fun getBusPositions(route: BusRoute): BusRoutePositions =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val stations = async { busStationInfoClientMap[route.serviceRegion]!!.getByRoute(route) }
                val positions = async { busPositionFetcherMap[route.serviceRegion]!!.fetch(route.id) }
                BusRoutePositions(
                    stations.await()
                        ?: throw TransitException.of(
                            TransitError.BUS_ROUTE_STATION_LIST_FETCH_FAILED,
                            "버스 노선 '${route.name}'의 경유 정류소 리스트를 가져오는데 실패했습니다."
                        ),
                    positions.await().also {
                        if (it.isEmpty()) {
                            throw TransitException.of(
                                TransitError.NOT_FOUND_BUS_POSITION,
                                "버스 노선 '${route.name}'의 버스 위치 정보를 찾을 수 없습니다."
                            )
                        }
                    }
                )
            }
        }

    private fun findStationAndRoute(
        routeName: String,
        meta: BusStationMeta
    ): Pair<BusStation, BusRoute> {
        val region = regionIdentifier.identify(meta.coordinate)

        val station =
            busStationInfoClientMap[region]
                ?.getStationByName(meta)
                ?: run {
                    log.warn { "$region - $meta 정류장 정보 실패" }
                    throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_STATION,
                        "$region 지역에서 버스 정류소 '${meta.name}'을 찾을 수 없습니다."
                    )
                }

        val route =
            busStationInfoClientMap[region]
                ?.getRoute(station, routeName)
                ?: run {
                    log.warn { "$region - 버스 노선($routeName) 정보 실패" }
                    throw TransitException.of(
                        TransitError.NOT_FOUND_BUS_ROUTE,
                        "$region 지역에서 버스 노선 '$routeName'을 찾을 수 없습니다."
                    )
                }

        return station to route
    }
}
