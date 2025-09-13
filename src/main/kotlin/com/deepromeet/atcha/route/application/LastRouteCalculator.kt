package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteTimeAdjuster
import com.deepromeet.atcha.route.domain.RouteItinerary
import com.deepromeet.atcha.route.exception.RouteError
import com.deepromeet.atcha.route.exception.RouteException
import com.deepromeet.atcha.route.infrastructure.cache.LastRouteMetricsRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.Collections

private val log = KotlinLogging.logger {}
private const val MAX_CALCULATION_TIME = 15_000L

@Component
class LastRouteCalculator(
    private val legCalculator: LastRouteLegCalculator,
    private val timeAdjuster: LastRouteTimeAdjuster,
    private val lastRouteAppender: LastRouteAppender,
    private val metricsRepository: LastRouteMetricsRepository
) {
    suspend fun calcLastRoutes(
        start: Coordinate,
        destination: Coordinate,
        itineraries: List<RouteItinerary>
    ): List<LastRoute> {
        metricsRepository.incrTotal(itineraries.size.toLong())
        val routes =
            coroutineScope {
                itineraries
                    .map { itinerary ->
                        async(Dispatchers.Default) {
                            withTimeoutOrNull(MAX_CALCULATION_TIME) {
                                calculateRoute(itinerary)
                            }
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }

        if (routes.isEmpty()) {
            throw RouteException.of(RouteError.LAST_ROUTES_NOT_FOUND)
        }

        metricsRepository.incrSuccess(routes.size.toLong())
        lastRouteAppender.appendRoutes(start, destination, routes)
        log.info { "총 ${itineraries.size}개의 여정 중 ${routes.size}개의 막차 경로를 계산했습니다." }
        return routes
    }

    fun streamLastRoutes(
        start: Coordinate,
        destination: Coordinate,
        itineraries: List<RouteItinerary>
    ): Flow<LastRoute> {
        val lastRouteBuffer = Collections.synchronizedList(mutableListOf<LastRoute>())
        metricsRepository.incrTotal(itineraries.size.toLong())

        val calculationTasks =
            itineraries.map { itinerary ->
                backgroundCalculationScope.async {
                    val route =
                        withTimeoutOrNull(MAX_CALCULATION_TIME) {
                            calculateRoute(itinerary)
                        }
                    if (route != null) {
                        lastRouteBuffer.add(route)
                    }
                    route
                }
            }

        handleRouteCalculationResults(calculationTasks, lastRouteBuffer, start, destination)

        return channelFlow {
            calculationTasks.forEach { job ->
                launch {
                    val route = job.await()
                    if (route != null && route.departureDateTime.isAfter(LocalDateTime.now())) {
                        send(route)
                    }
                }
            }
        }
    }

    private suspend fun calculateRoute(itinerary: RouteItinerary): LastRoute? =
        runCatching {
            val calculatedLegs = legCalculator.calcWithLastTime(itinerary.legs)
            val timeAdjustedLegs = timeAdjuster.adjustTransitDepartureTimes(calculatedLegs)
            LastRoute.create(itinerary, timeAdjustedLegs)
        }.onFailure { exception ->
            log.warn(exception) { "여정의 막차 시간 계산 중 예외가 발생하여 해당 여정을 제외합니다." }
        }.getOrNull()

    private fun handleRouteCalculationResults(
        calculationTasks: List<Deferred<LastRoute?>>,
        lastRouteBuffer: List<LastRoute>,
        start: Coordinate,
        destination: Coordinate
    ) {
        backgroundCalculationScope.launch {
            calculationTasks.awaitAll()
            log.info { "백그라운드 계산 완료 - ${lastRouteBuffer.size}개 캐싱" }
            metricsRepository.incrSuccess(lastRouteBuffer.size.toLong())

            if (lastRouteBuffer.isNotEmpty()) {
                lastRouteAppender.appendRoutes(start, destination, lastRouteBuffer.toList())
            }
        }
    }

    private val backgroundCalculationScope =
        CoroutineScope(
            Dispatchers.Default +
                CoroutineName("LastRouteCalculation")
        )
}
