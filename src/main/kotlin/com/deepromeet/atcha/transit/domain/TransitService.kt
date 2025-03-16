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
import java.time.LocalDateTime

@Service
class TransitService(
    private val taxiFareFetcher: TaxiFareFetcher,
    private val busManager: BusManager,
    private val subwayManager: SubwayManager,
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
    ): BusArrival? {
        return busManager.getArrivalInfo(routeName, BusStationMeta(stationName, coordinate))
    }

    fun getTaxiFare(
        start: Coordinate,
        end: Coordinate
    ): Fare {
        return taxiFareFetcher.fetch(start, end) ?: throw TransitException.TaxiFareFetchFailed
    }

    fun getLastTime(
        subwayLine: SubwayLine,
        startStationName: String,
        endStationName: String
    ): SubwayTime? {
        val routes = subwayManager.getRoutes(subwayLine)
        val startStation = subwayManager.getStation(subwayLine, startStationName)
        val endStation = subwayManager.getStation(subwayLine, endStationName)
        return subwayManager.getTimeTable(startStation, endStation, routes)?.getLastTime(endStation, routes)
    }

    fun getRoute(routeId: String): LastRoutesResponse {
        return lastRouteReader.read(routeId)
    }

    fun getDepartureRemainingTime(routeId: String): Int {
        return lastRouteReader.readRemainingTime(routeId)
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
                return routeIds.map { routeId -> lastRouteReader.read(routeId) }
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

        val now = LocalDateTime.now()
        val filteredRoutes = lastRouteOperations.getFilteredRoutes(lastRoutesResponses, now)

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
