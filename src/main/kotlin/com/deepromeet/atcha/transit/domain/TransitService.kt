package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
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

    fun getBusArrivalInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival {
        return busManager.getArrivalInfo(routeName, busStationMeta)
            ?: throw TransitException.NotFoundBusArrival
    }

    fun getBusArrivalInfoV2(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival {
        return busManager.getArrivalInfoV2(routeName, busStationMeta)
            ?: throw TransitException.NotFoundBusArrival
    }

    suspend fun getBusPositions(busRoute: BusRoute) = busManager.getBusPositions(busRoute)

    fun getBusOperationInfo(busRoute: BusRoute): BusRouteOperationInfo {
        return busManager.getBusRouteOperationInfo(busRoute)
    }

    fun getTaxiFare(
        start: Coordinate,
        end: Coordinate
    ): Fare {
        return taxiFareFetcher.fetch(start, end) ?: Fare(0)
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
        end: Coordinate?,
        sortType: LastRouteSortType
    ): List<LastRoute> {
        // 목적지 설정 안 하면 집 주소임
        val destination = end ?: userReader.read(userId).getHomeCoordinate()

        // 캐시에서 조회, 있을 경우 해당 경로를 리턴
        // TODO - 준원 : 현재 캐시에서 같은 출발지와 목적지만 캐싱처리가 되는데, 버스 번호에 따라서 할 수 있는지 체크해보기
        lastRouteReader.read(start, destination)?.let { routes ->
            return routes.sort(sortType)
        }

        // 캐시에서 조회가 안 될 경우, Tmap API를 통해서 경로를 조회
        val itineraries = transitRouteClient.fetchItineraries(start, destination)

        return lastRouteOperations
            .calculateRoutes(start, destination, itineraries)
            .sort(sortType)
    }

    suspend fun getLastRoutesV2(
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
            .calculateRoutesV2(
                start,
                destination,
                itineraries.map {
                    Itinerary(
                        it.totalTime,
                        it.transferCount,
                        it.totalWalkDistance,
                        it.totalDistance,
                        it.totalWalkTime,
                        it.fare,
                        it.legs,
                        it.pathType
                    )
                }
            )
            .sort(sortType)
    }
}
