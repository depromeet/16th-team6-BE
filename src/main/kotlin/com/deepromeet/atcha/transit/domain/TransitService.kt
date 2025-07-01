package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.user.domain.UserReader
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
    private val transitRouteClientV2: TransitRouteClientV2,
    private val lastRouteOperationsV2: LastRouteOperationsV2,
    private val regionIdentifier: RegionIdentifier,
    private val serviceRegionValidator: ServiceRegionValidator
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
        val itineraries = transitRouteClientV2.fetchItinerariesV2(start, destination)
        return lastRouteOperationsV2
            .calculateRoutesV2(start, destination, itineraries)
            .sort(sortType)
    }

    fun getRoute(routeId: String): LastRoute {
        return lastRouteReader.read(routeId)
    }

    fun getBusArrival(
        routeName: String,
        busStationMeta: BusStationMeta,
        nextStationName: String? = null
    ): BusArrival {
        val schedule = busManager.getSchedule(routeName, busStationMeta, nextStationName)
        val realTimeArrival = busManager.getRealTimeArrival(routeName, busStationMeta, nextStationName)
        return BusArrival(schedule, realTimeArrival)
    }

    suspend fun getBusPositions(busRoute: BusRoute) = busManager.getBusPositions(busRoute)

    fun getBusOperationInfo(busRoute: BusRoute): BusRouteOperationInfo {
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
        val busPositions = busManager.getBusPositions(busInfo.busRoute)

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

    private fun validateServiceRegion(
        start: Coordinate,
        destination: Coordinate
    ) {
        regionIdentifier.identify(start)
        regionIdentifier.identify(destination)
    }
}
