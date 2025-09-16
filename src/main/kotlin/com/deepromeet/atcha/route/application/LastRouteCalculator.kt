package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteTimeAdjuster
import com.deepromeet.atcha.route.domain.RouteItinerary
import com.deepromeet.atcha.route.domain.isValidLastRoute
import com.deepromeet.atcha.route.exception.RouteError
import com.deepromeet.atcha.route.exception.RouteException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

private val log = KotlinLogging.logger {}
private const val MAX_CALCULATION_TIME = 15_000L

@Component
class LastRouteCalculator(
    private val legCalculator: LastRouteLegCalculator,
    private val timeAdjuster: LastRouteTimeAdjuster,
    private val lastRouteAppender: LastRouteAppender
) {
    @Deprecated("deprecated")
    suspend fun calcLastRoutes(
        start: Coordinate,
        destination: Coordinate,
        itineraries: List<RouteItinerary>
    ): List<LastRoute> {
        val routes =
            coroutineScope {
                itineraries
                    .map { itinerary ->
                        async(Dispatchers.Default) {
                            withTimeoutOrNull(MAX_CALCULATION_TIME) {
                                calculateRoute(itinerary)
                                    ?.takeIf { it.isValidLastRoute() }
                            }
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }

        if (routes.isEmpty()) {
            throw RouteException.of(RouteError.LAST_ROUTES_NOT_FOUND)
        }

        lastRouteAppender.appendRoutes(start, destination, routes)
        log.info { "총 ${itineraries.size}개의 여정 중 ${routes.size}개의 유효 막차 경로를 계산했습니다." }
        return routes
    }

    fun streamLastRoutes(
        start: Coordinate,
        destination: Coordinate,
        itineraries: List<RouteItinerary>
    ): Flow<LastRoute> =
        channelFlow {
            val lastRouteBuffer = ConcurrentLinkedQueue<LastRoute>()
            val calculationTasks = createCalculationTasks(itineraries, lastRouteBuffer)
            calculationTasks.handleResultsInBackground(lastRouteBuffer, start, destination)
            sendRoutes(calculationTasks)
        }

    private fun createCalculationTasks(
        itineraries: List<RouteItinerary>,
        lastRouteBuffer: ConcurrentLinkedQueue<LastRoute>
    ): List<Deferred<LastRoute?>> =
        itineraries.map { itinerary ->
            backgroundCalculationScope.async(Dispatchers.Default) {
                calculateValidRoute(itinerary, lastRouteBuffer)
            }
        }

    private suspend fun calculateValidRoute(
        itinerary: RouteItinerary,
        buffer: ConcurrentLinkedQueue<LastRoute>
    ): LastRoute? {
        val route =
            withTimeoutOrNull(MAX_CALCULATION_TIME) {
                calculateRoute(itinerary)
            }

        return route?.takeIf { it.isValidLastRoute() }
            ?.also { buffer.offer(it) }
    }

    private suspend fun calculateRoute(itinerary: RouteItinerary): LastRoute? =
        runCatching {
            val calculatedLegs = legCalculator.calcLastTime(itinerary.legs)
            val timeAdjustedLegs = timeAdjuster.adjustTransitDepartureTimes(calculatedLegs)
            LastRoute.create(itinerary, timeAdjustedLegs)
        }.onFailure { exception ->
            log.warn(exception) { "여정의 막차 시간 계산 중 예외가 발생하여 해당 여정을 제외합니다." }
        }.getOrNull()

    private fun List<Deferred<LastRoute?>>.handleResultsInBackground(
        lastRouteBuffer: ConcurrentLinkedQueue<LastRoute>,
        start: Coordinate,
        destination: Coordinate
    ) {
        backgroundCalculationScope.launch {
            this@handleResultsInBackground.awaitAll()
            log.info { "총 ${this@handleResultsInBackground.size}개의 여정 중 ${lastRouteBuffer.size}개의 유효 막차 경로를 계산했습니다." }
            if (lastRouteBuffer.isNotEmpty()) {
                lastRouteAppender.appendRoutes(start, destination, lastRouteBuffer.toList())
            }
        }
    }

    private fun ProducerScope<LastRoute>.sendRoutes(calculationTasks: List<Deferred<LastRoute?>>) {
        calculationTasks.forEach { job ->
            launch { job.await()?.let { send(it) } }
        }
    }

    private val backgroundCalculationScope =
        CoroutineScope(
            Dispatchers.Default +
                CoroutineName("LastRouteCalculation")
        )
}
