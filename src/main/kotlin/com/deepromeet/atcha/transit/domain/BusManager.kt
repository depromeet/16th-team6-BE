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
                .logIfNull(
                    "[NotFoundBusStation] region=$region," +
                        " station=${busStationMeta.resolveName()}"
                )
                ?: return null
        val busRoute =
            busStationInfoClientMap[region]?.getRoute(station, routeName)
                .logIfNull(
                    "[NotFoundBusRoute] region=$region," +
                        " station=${station.busStationMeta.name}, routeName=$routeName"
                )
                ?: return null

//        얘만 오딧세이로 바꾸면 되긴 함
        val busArrival =
            busRouteInfoClientMap[region]?.getBusArrival(station, busRoute)
                .logIfNull(
                    "[NotFoundBusArrival] region=$region, " +
                        "station=${station.busStationMeta.name}, routeName=$routeName"
                )
        if (busArrival != null) {
            busTimeTableCache.cache(routeName, busStationMeta, busArrival.busTimeTable)
        }

        return busArrival
    }

    fun getArrivalInfoV2(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival? {
        println("v2 여기 잘 들어옴")
//        지역 정보 TMap에서 가져옴
        val region = regionIdentifier.identify(busStationMeta.coordinate)

//        버스 정류장 정보를 '지역'을 키 값으로 가져옴
//        데이터 정합성이 걱정돼서 여기부터 아래 3개는 오딧세이로 해보는 게 좋지 않을까라는 생각이 듦
        val station =
            busStationInfoClientMap[region]?.getStationByName(busStationMeta)
                .logIfNull(
                    "[NotFoundBusStation] region=$region," +
                        " station=${busStationMeta.resolveName()}"
                )
                ?: return null
        val busRoute =
            busStationInfoClientMap[region]?.getRoute(station, routeName)
                .logIfNull(
                    "[NotFoundBusRoute] region=$region," +
                        " station=${station.busStationMeta.name}, routeName=$routeName"
                )
                ?: return null

//        얘만 오딧세이로 바꾸면 되긴 함
        var busArrival =
            busRouteInfoClientMap[region]?.getBusArrival(station, busRoute)
                ?.busTimeTable?.lastTime?.let {
                    oDSayBusInfoClient.getBusArrival(station, busRoute)
                        .logIfNull(
                            "[ODSay NotFoundBusArrival] region=$region, " +
                                "station=${station.busStationMeta.name}, routeName=$routeName"
                        )
                }

//        TODO : ㅇㅇㅇㅇㅇㅇㅇㅇ 여기부터 트러블 슈팅 시작. null로 나오는 중임
        if (busArrival != null) {
            busTimeTableCache.cache(routeName, busStationMeta, busArrival.busTimeTable)
        }

        return busArrival
    }

    fun getBusTimeInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusTimeTable? {
//        캐시에 있으면 가져옴
        return busTimeTableCache.get(routeName, busStationMeta)
            ?: getArrivalInfo(routeName, busStationMeta)?.busTimeTable
    }

    fun getBusTimeInfoV2(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusTimeTable? {
//        캐시에 있으면 가져옴
        return busTimeTableCache.get(routeName, busStationMeta)
            ?: getArrivalInfoV2(routeName, busStationMeta)?.busTimeTable
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

fun <T> T?.logIfNull(message: String): T? {
    if (this == null) {
        log.warn { message }
    }
    return this
}
