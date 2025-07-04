package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.infrastructure.client.tmap.TransitRouteClientV2
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.PassStopList
import com.deepromeet.atcha.user.domain.UserReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service

@Service
class TransitService(
    private val taxiFareFetcher: TaxiFareFetcher,
    private val busManager: BusManager,
    private val subwayStationBatchAppender: SubwayStationBatchAppender,
    private val userReader: UserReader,
    private val transitRouteSearchClient: TransitRouteSearchClient,
    private val lastRouteReader: LastRouteReader,
    private val lastRouteOperations: LastRouteOperations,
    private val startedBusCache: StartedBusCache,
    private val serviceRegionValidator: ServiceRegionValidator,
    private val lastRouteOperationsV2: LastRouteOperationsV2,
    private val transitRouteClientV2: TransitRouteClientV2
) {
    fun getTaxiFare(
        start: Coordinate,
        end: Coordinate
    ): Fare {
        return taxiFareFetcher.fetch(start, end)
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
        serviceRegionValidator.validate(start, destination)
        val itineraries = transitRouteSearchClient.searchRoutes(start, destination)
        val validItineraries = ItineraryValidator.filterValidItineraries(itineraries)
        return lastRouteOperations
            .calculateLastRoutes(start, destination, validItineraries)
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
        serviceRegionValidator.validate(start, destination)
        val itineraries = transitRouteClientV2.fetchItinerariesV2(start, destination)
        val validItineraries = ItineraryValidator.filterValidItineraries(itineraries)
        return lastRouteOperationsV2
            .calculateRoutesV2(start, destination, validItineraries)
            .sort(sortType)
    }

    fun streamLastRoutes(
        userId: Long,
        start: Coordinate,
        end: Coordinate?,
        sortType: LastRouteSortType
    ): Flow<LastRoute> =
        flow {
            val destination = end ?: userReader.read(userId).getHomeCoordinate()

            lastRouteReader.read(start, destination)?.let { cached ->
                cached.sort(sortType).forEach { emit(it) }
                return@flow
            }

            val itineraries = transitRouteSearchClient.searchRoutes(start, destination)

            lastRouteOperations.streamLastRoutes(start, destination, itineraries)
                .collect { route -> emit(route) }
        }

    fun getRoute(routeId: String): LastRoute {
        return lastRouteReader.read(routeId)
    }

    suspend fun getBusArrival(
        routeName: String,
        busStationMeta: BusStationMeta,
        passStopList: PassStopList
    ): BusArrival {
        val schedule = busManager.getSchedule(routeName, busStationMeta, passStopList)
        val realTimeArrival = busManager.getRealTimeArrival(routeName, busStationMeta, passStopList)
        return BusArrival(schedule, realTimeArrival)
    }

    suspend fun getBusPositions(busRoute: BusRoute) = busManager.getBusPositions(busRoute)

    suspend fun getBusOperationInfo(busRoute: BusRoute): BusRouteOperationInfo {
        return busManager.getBusRouteOperationInfo(busRoute)
    }

    fun getDepartureRemainingTime(routeId: String): Int {
        return lastRouteReader.readRemainingTime(routeId)
    }

    suspend fun isBusStarted(lastRouteId: String): Boolean {
        startedBusCache.get(lastRouteId)?.let { return true }

        val lastRoute = lastRouteReader.read(lastRouteId)
        val firstBus = lastRoute.findFirstBus()
        val busInfo = firstBus.transitInfo as TransitInfo.BusInfo

        val busPositions =
            runCatching {
                busManager.getBusPositions(busInfo.busRoute)
            }.getOrElse { return false }

        busPositions.findTargetBus(
            busInfo.busStation,
            firstBus.departureDateTime!!,
            busInfo.timeTable.term
        )?.let {
            startedBusCache.cache(lastRouteId, it)
            return true
        }

        return false
    }

    fun init() {
        subwayStationBatchAppender.appendAll()
    }
}
