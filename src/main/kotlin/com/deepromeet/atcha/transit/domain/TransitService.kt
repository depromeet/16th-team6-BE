package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
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
        stationName: String,
        coordinate: Coordinate
    ): BusArrival {
        return busManager.getArrivalInfo(routeName, BusStationMeta(stationName, coordinate))
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
        return taxiFareFetcher.fetch(start, end) ?: throw TransitException.TaxiFareFetchFailed
    }

    fun getRoute(routeId: String): LastRoutesResponse {
        return lastRouteReader.read(routeId)
    }

    fun getDepartureRemainingTime(routeId: String): Int {
        return lastRouteReader.readRemainingTime(routeId)
    }

    fun isBusStarted(
        userId: Long,
        routeName: String,
        busStationMeta: BusStationMeta
    ): Boolean {
        // 1. 사용자 ID를 통해 등록된 알림에서 경로를 조회하여 예상 출발 시간을 가져옴
        // 2. 버스 도착 정보를 조회
        // 3. 버스 실시간 정보에서 2번째 버스의 예상 도착시간이 1번에서 가져온 예상 출발 시간과 비교하여 버스가 출발했는지 확인
        return false
    }

    suspend fun getLastRoutes(
        userId: Long,
        start: Coordinate,
        endLat: String?,
        endLon: String?
    ): List<LastRoutesResponse> {
        // end 가 없는 경우, 사용자 집 주소 조회
        val end =
            if (endLat == null || endLon == null) {
                Coordinate(
                    userReader.read(userId).address.lat,
                    userReader.read(userId).address.lon
                )
            } else {
                Coordinate(endLat.toDouble(), endLon.toDouble())
            }

        // 출발지와 도착지 경로가 redis 에 저장된 경우
        lastRouteIndexReader.read(start, end).let { routeIds ->
            if (routeIds.isNotEmpty()) {
                return lastRouteOperations.sortedByMinTransfer(
                    lastRouteOperations.getFilteredRoutes(
                        routeIds.map { routeId -> lastRouteReader.read(routeId) }
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

        return lastRouteOperations.sortedByMinTransfer(filteredRoutes)
    }

    private fun saveRouteIdsByStartEnd(
        start: Coordinate,
        end: Coordinate,
        routeIds: List<String>
    ) {
        lastRouteIndexAppender.append(start, end, routeIds)
    }

    private fun saveRoutesToCache(routes: List<LastRoutesResponse>) {
        routes.forEach { route -> lastRouteAppender.append(route) }
    }
}
