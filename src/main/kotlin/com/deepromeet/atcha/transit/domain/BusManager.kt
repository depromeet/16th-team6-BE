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
    ): BusArrival? {
//        지역 정보 TMap에서 가져옴
        val region = regionIdentifier.identify(busStationMeta.coordinate)

//        버스 정류장 정보를 '지역'을 키 값으로 가져옴
//        데이터 정합성이 걱정돼서 여기부터 아래 3개는 오딧세이로 해보는 게 좋지 않을까라는 생각이 듦
        val station =
            busStationInfoClientMap[region]?.getStationByName(busStationMeta)
                ?: return null
        val busRoute =
            busStationInfoClientMap[region]?.getRoute(station, routeName)
                ?: return null

        val busArrival =
            busRouteInfoClientMap[region]?.getBusArrival(station, busRoute)
        if (busArrival != null) {
            busTimeTableCache.cache(routeName, busStationMeta, busArrival.busTimeTable)
        }

        return busArrival
    }

    fun getArrivalInfoV2(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival? {
        val region = regionIdentifier.identify(busStationMeta.coordinate)

        val station =
            busStationInfoClientMap[region]?.getStationByName(busStationMeta)
                ?: return null
        val busRoute =
            busStationInfoClientMap[region]?.getRoute(station, routeName)
                ?: return null

        val busArrival =
            busRouteInfoClientMap[region]?.getBusArrival(station, busRoute)
                ?: run {
                    log.info { "오픈API에서 버스도착정보를 가져올 수 없어 오디세이를 이용합니다." }
                    oDSayBusInfoClient.getBusArrival(station, busRoute)
                }

        if (busArrival != null) {
            busTimeTableCache.cache(routeName, busStationMeta, busArrival.busTimeTable)
        }

        return busArrival
    }

    fun getBusTimeInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusTimeTable? {
        return getArrivalInfo(routeName, busStationMeta)?.busTimeTable
    }

    fun getBusTimeInfoV2(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusTimeTable? {
        return getArrivalInfoV2(routeName, busStationMeta)?.busTimeTable
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
