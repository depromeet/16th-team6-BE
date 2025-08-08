package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.ItineraryValidator
import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteSortType
import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.route.domain.sort
import com.deepromeet.atcha.route.infrastructure.client.tmap.TransitRouteClientV2
import com.deepromeet.atcha.transit.application.TransitRouteSearchClient
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.application.bus.StartedBusCache
import com.deepromeet.atcha.transit.application.region.ServiceRegionValidator
import com.deepromeet.atcha.user.application.UserReader
import com.deepromeet.atcha.user.domain.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service

@Service
class RouteService(
    private val lastRouteReader: LastRouteReader,
    private val userReader: UserReader,
    private val userRouteManager: UserRouteManager,
    private val transitRouteSearchClient: TransitRouteSearchClient,
    private val lastRouteCalculator: LastRouteCalculator,
    private val serviceRegionValidator: ServiceRegionValidator,
    private val startedBusCache: StartedBusCache,
    private val busManager: BusManager,
    private val userRouteDepartureTimeRefresher: UserRouteDepartureTimeRefresher,
    private val lastRouteCalculatorV2: LastRouteCalculatorV2,
    private val transitRouteClientV2: TransitRouteClientV2
) {
    suspend fun getLastRoutes(
        userId: UserId,
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
        return lastRouteCalculator
            .calcLastRoutes(start, destination, validItineraries)
            .sort(sortType)
    }

    suspend fun getLastRoutesForTest(
        userId: UserId,
        start: Coordinate,
        end: Coordinate?,
        sortType: LastRouteSortType
    ): List<LastRoute> {
        val destination = end ?: userReader.read(userId).getHomeCoordinate()
        serviceRegionValidator.validate(start, destination)
        val itineraries = transitRouteClientV2.fetchItinerariesV2(start, destination)
        val validItineraries = ItineraryValidator.filterValidItineraries(itineraries)
        return lastRouteCalculatorV2
            .calculateRoutesV2(start, destination, validItineraries)
            .sort(sortType)
    }

    fun getLastRouteStream(
        userId: UserId,
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
            val validItineraries = ItineraryValidator.filterValidItineraries(itineraries)
            lastRouteCalculator.streamLastRoutes(start, destination, validItineraries)
                .collect { route -> emit(route) }
        }

    fun getRoute(routeId: String): LastRoute {
        return lastRouteReader.read(routeId)
    }

    suspend fun isBusStarted(lastRouteId: String): Boolean {
        startedBusCache.get(lastRouteId)?.let { return true }
        val lastRoute = lastRouteReader.read(lastRouteId)

        val firstBus = lastRoute.findFirstBus()
        val busInfo = firstBus?.busInfo ?: return false
        val departureDateTime = firstBus.departureDateTime ?: return false

        val locatedBus = busManager.locateBus(busInfo, departureDateTime) ?: return false

        startedBusCache.cache(lastRouteId, locatedBus)
        return true
    }

    fun getDepartureRemainingTime(routeId: String): Int {
        return lastRouteReader.readRemainingTime(routeId)
    }

    fun addUserRoute(
        id: UserId,
        lastRouteId: String
    ) {
        val user = userReader.read(id)
        val route = lastRouteReader.read(lastRouteId)
        userRouteManager.update(user, route)
    }

    fun deleteUserRoute(id: UserId) {
        val user = userReader.read(id)
        userRouteManager.delete(user)
    }

    suspend fun refreshUserRoute(id: UserId): UserRoute {
        val user = userReader.read(id)
        val userRoute = userRouteManager.read(user)
        return userRouteDepartureTimeRefresher.refreshDepartureTime(userRoute)
            ?: userRoute
    }
}
