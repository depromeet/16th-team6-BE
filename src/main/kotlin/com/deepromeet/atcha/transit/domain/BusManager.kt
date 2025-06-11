package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.odsay.ODSayBusInfoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    fun getArrivalInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival {
        val region = regionIdentifier.identify(busStationMeta.coordinate)

        val station =
            busStationInfoClientMap[region]?.getStationByName(busStationMeta)
                ?: run {
                    log.warn { "$region - $busStationMeta 의 정보를 가져오는데 실패" }
                    throw TransitException.NotFoundBusStation
                }
        val busRoute =
            busStationInfoClientMap[region]?.getRoute(station, routeName)
                ?: run {
                    log.warn { "$region - 버스 노선($routeName) 정보를 가져오는데 실패" }
                    throw TransitException.NotFoundBusRoute
                }

        val busArrival =
            busRouteInfoClientMap[region]?.getBusArrival(station, busRoute)
                ?: run {
                    log.debug { "오픈API에서 버스도착정보($station-$busRoute)를 가져올 수 없어 오디세이를 이용합니다." }
                    oDSayBusInfoClient.getBusArrival(station, busRoute)
                } ?: throw TransitException.NotFoundBusArrival

        busTimeTableCache.cache(routeName, busStationMeta, busArrival.busTimeTable)

        return busArrival
    }

    fun getBusRealTimeInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): List<RealTimeBusArrival> {
        TODO()
    }

    fun getBusTimeInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusTimeTable {
        return getArrivalInfo(routeName, busStationMeta).busTimeTable
    }

    fun getBusRouteOperationInfo(route: BusRoute): BusRouteOperationInfo {
        return busRouteInfoClientMap[route.serviceRegion]!!.getBusRouteInfo(route)
            ?: throw TransitException.BusRouteOperationInfoFetchFailed
    }

    suspend fun getBusPositions(busRoute: BusRoute): BusRoutePositions =
        coroutineScope {
            val stationListDeferred =
                async(Dispatchers.IO) {
                    busStationInfoClientMap[busRoute.serviceRegion]!!
                        .getByRoute(busRoute)
                        ?: throw TransitException.BusRouteStationListFetchFailed
                }

            val positionsDeferred =
                async(Dispatchers.IO) {
                    busPositionFetcherMap[busRoute.serviceRegion]!!
                        .fetch(busRoute.id)
                }

            BusRoutePositions(stationListDeferred.await(), positionsDeferred.await())
        }
}
