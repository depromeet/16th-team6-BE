package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service

@Service
class TransitService(
    private val taxiFareFetcher: TaxiFareFetcher,
    private val busManager: BusManager,
    private val subwayStationBatchAppender: SubwayStationBatchAppender,
    private val userReader: UserReader,
    private val transitRouteClient: TransitRouteClient,
    private val lastRouteReader: LastRouteReader,
    private val lastRouteOperations: LastRouteOperations
) {
    fun init() {
        subwayStationBatchAppender.appendAll()
    }

    fun getBusArrival(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival {
        val schedule = busManager.getSchedule(routeName, busStationMeta)
        val realTimeArrival = busManager.getRealTimeArrival(routeName, busStationMeta)
        return BusArrival(schedule, realTimeArrival)
    }

    suspend fun getBusPositions(busRoute: BusRoute) = busManager.getBusPositions(busRoute)

    fun getBusOperationInfo(busRoute: BusRoute): BusRouteOperationInfo {
        return busManager.getBusRouteOperationInfo(busRoute)
    }

    fun getTaxiFare(
        start: Coordinate,
        end: Coordinate
    ): Fare {
        return taxiFareFetcher.fetch(start, end)
            ?: throw TransitException.of(
                TransitError.TAXI_FARE_FETCH_FAILED,
                "출발지(${start.lat}, ${start.lon})에서 도착지(${end.lat}, ${end.lon})까지의 택시 요금을 조회할 수 없습니다."
            )
    }

    fun getRoute(routeId: String): LastRoute {
        return lastRouteReader.read(routeId)
    }

    fun getDepartureRemainingTime(routeId: String): Int {
        return lastRouteReader.readRemainingTime(routeId)
    }

    // TODO: 두번째 도착 예정 버스인지 확인하고있음 -> 실제 차고지 출발 여부 확인으로 변경 필요
    fun isBusStarted(lastRouteId: String): Boolean {
        val lastRoute = lastRouteReader.read(lastRouteId)
        val busRealTime =
            busManager.getRealTimeArrival(
                lastRoute.findFirstBus().resolveRouteName(),
                lastRoute.findFirstBus().resolveStartStation()
            )
        return busRealTime.getSecondBus()?.isTargetBus(lastRoute.findFirstBus()) ?: false
    }

    suspend fun getLastRoutes(
        userId: Long,
        start: Coordinate,
        end: Coordinate?,
        sortType: LastRouteSortType
    ): List<LastRoute> {
        val destination = end ?: userReader.read(userId).getHomeCoordinate()
        lastRouteReader.read(start, destination)?.let { routes ->
            return routes.sort(sortType)
        }
        val itineraries = transitRouteClient.fetchItineraries(start, destination)
        return lastRouteOperations
            .calculateRoutes(start, destination, itineraries)
            .sort(sortType)
    }
}
