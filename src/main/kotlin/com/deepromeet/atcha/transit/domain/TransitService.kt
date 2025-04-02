package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.api.response.LastRoutes
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.user.domain.UserReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class TransitService(
    private val taxiFareFetcher: TaxiFareFetcher,
    private val busManager: BusManager,
    private val subwayStationBatchAppender: SubwayStationBatchAppender,
    private val regionIdentifier: RegionIdentifier,
    private val userReader: UserReader,
    private val lastRouteReader: LastRouteReader,
    private val lastRouteAppender: LastRouteAppender,
    private val lastRouteIndexReader: LastRouteIndexReader,
    private val lastRouteIndexAppender: LastRouteIndexAppender,
    private val lastRouteOperations: LastRouteOperations
) {
    fun init() {
        subwayStationBatchAppender.appendAll()
    }

    fun getBusArrivalInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival {
        return busManager.getArrivalInfo(routeName, busStationMeta)
            ?: throw TransitException.NotFoundBusArrival
    }

    fun getBusPositions(busRoute: BusRoute): BusRoutePositions {
        val busRouteStationList = busManager.getBusRouteStationList(busRoute)
        val busPositions = busManager.getBusPosition(busRoute)
        return BusRoutePositions(busRouteStationList, busPositions)
    }

    fun getBusOperationInfo(busRoute: BusRoute): BusRouteOperationInfo {
        return busManager.getBusRouteOperationInfo(busRoute)
    }

    fun getTaxiFare(
        start: Coordinate,
        end: Coordinate
    ): Fare {
        return taxiFareFetcher.fetch(start, end) ?: Fare(0)
    }

    fun getRoute(routeId: String): LastRoutes {
        return lastRouteReader.read(routeId)
    }

    fun getDepartureRemainingTime(routeId: String): Int {
        return lastRouteReader.readRemainingTime(routeId)
    }

    // TODO: 두번째 도착 예정 버스인지 확인하고있음 -> 실제 차고지 출발 여부 확인으로 변경 필요
    fun isBusStarted(lastRouteId: String): Boolean {
        val lastRoute = lastRouteReader.read(lastRouteId)
        val busArrival =
            busManager.getArrivalInfo(
                lastRoute.findFirstBus().resolveRouteName(),
                lastRoute.findFirstBus().resolveStartStation()
            )
        return busArrival?.getSecondBus()?.isTargetBus(lastRoute.findFirstBus()) ?: false
    }

    suspend fun getLastRoutes(
        userId: Long,
        start: Coordinate,
        endLat: String?,
        endLon: String?,
        sortType: LastRouteSortType
    ): List<LastRoutes> {
        // end 가 없는 경우, 사용자 집 주소 조회
        val end = getDeparture(endLat, endLon, userId)

        // 출발지와 도착지 경로가 redis 에 저장된 경우
        lastRouteIndexReader.read(start, end).let { routeIds ->
            if (routeIds.isNotEmpty()) {
                return lastRouteOperations.sort(
                    sortType,
                    lastRouteOperations.getFilteredRoutes(
                        routeIds.map { routeId ->
                            lastRouteReader.read(routeId)
                        }
                    )
                )
            }
        }

        // 서비스 지역인지 판별 -> 서비스 지역이 아니면 Exception 발생
        regionIdentifier.identify(start)
        regionIdentifier.identify(end)

        // 막차 조회
        val allRoutes = lastRouteOperations.getItineraries(start, end)
        val deduplicatedRoutes = lastRouteOperations.filterAndDeduplicateItineraries(allRoutes)

        val lastRoutesResponses =
            coroutineScope {
                deduplicatedRoutes
                    .map { route ->
                        async(Dispatchers.Default) {
                            lastRouteOperations.calculateRoute(route)
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }

        val filteredRoutes = lastRouteOperations.getFilteredRoutes(lastRoutesResponses)

        saveRouteIdsByStartEnd(start, end, filteredRoutes.map { it.routeId })
        saveRoutesToCache(filteredRoutes)

        return lastRouteOperations.sort(sortType, filteredRoutes)
    }

    private fun getDeparture(
        endLat: String?,
        endLon: String?,
        userId: Long
    ) = if (endLat == null || endLon == null) {
        val user = userReader.read(userId)
        Coordinate(
            user.address.lat,
            user.address.lon
        )
    } else {
        Coordinate(endLat.toDouble(), endLon.toDouble())
    }

    private fun saveRouteIdsByStartEnd(
        start: Coordinate,
        end: Coordinate,
        routeIds: List<String>
    ) {
        lastRouteIndexAppender.append(start, end, routeIds)
    }

    private fun saveRoutesToCache(routes: List<LastRoutes>) {
        routes.forEach { route -> lastRouteAppender.append(route) }
    }
}
