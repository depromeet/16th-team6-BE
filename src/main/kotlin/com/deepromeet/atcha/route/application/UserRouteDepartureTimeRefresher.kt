package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrival
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val BUS_ARRIVAL_THRESHOLD_MINUTES = 3
private const val FIXED_REFRESH_MINUTES = 20
private const val BUFFER_SEC_SECONDS = 2 * 60L

data class OptimalDepartureTime(
    val busArrivalTime: LocalDateTime,
    val routeDepartureTime: LocalDateTime
)

@Component
class UserRouteDepartureTimeRefresher(
    private val userRouteManager: UserRouteManager,
    private val lastRouteAppender: LastRouteAppender,
    private val busManager: BusManager,
    private val lastRouteReader: LastRouteReader
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun refreshAll(): List<UserRoute> =
        userRouteManager.readAll().mapNotNull { userRoute ->
            refreshDepartureTime(userRoute)
        }

    suspend fun refreshDepartureTime(userRoute: UserRoute): UserRoute? {
        val route = lastRouteReader.read(userRoute.lastRouteId)

        // 1) 버스 구간 및 시간표 추출
        val firstBusLeg = extractFirstBusTransit(route) ?: return null
        val timeTable = firstBusLeg.busInfo?.timeTable ?: return null

        if (isNotRefreshTarget(route.parseDepartureTime(), timeTable.term)) {
            return null
        }

        // 2) 실시간 도착 정보 조회
        val arrivalInfo =
            busManager.getRealTimeArrival(
                firstBusLeg.resolveRouteName(),
                firstBusLeg.toBusStationMeta(),
                firstBusLeg.passStops!!
            )

        // 3) 도착 후보 시각 계산 및 최적 출발시간 결정
        val optimalTime =
            calculateOptimalDepartureTime(
                arrivalInfo,
                timeTable,
                route
            ) ?: return null

        return saveUpdatedRoute(userRoute, route, firstBusLeg, optimalTime)
    }

    private fun extractFirstBusTransit(route: LastRoute): LastRouteLeg? {
        val firstTransit = route.findFirstTransit()
        return if (firstTransit.isBus()) firstTransit else null
    }

    /** "20분 + 배차 간격" 이내가 아니면 갱신 금지 */
    private fun isNotRefreshTarget(
        oldDeparture: LocalDateTime,
        busTerm: Int
    ): Boolean {
        val minutesLeft = Duration.between(LocalDateTime.now(), oldDeparture).toMinutes()
        return minutesLeft !in BUS_ARRIVAL_THRESHOLD_MINUTES until (FIXED_REFRESH_MINUTES + busTerm)
    }

    private fun calculateOptimalDepartureTime(
        arrivalInfo: BusRealTimeArrival,
        timeTable: BusTimeTable,
        route: LastRoute
    ): OptimalDepartureTime? {
        val now = LocalDateTime.now()
        val walkingTime = route.calcWalkingTimeToFirstTransit()
        val oldDepartureTime = route.parseDepartureTime()

        return arrivalInfo
            .createArrivalCandidates(timeTable.term)
            .filter { it.isBefore(timeTable.lastTime) }
            .mapNotNull { arrival ->
                arrival
                    .minusSeconds(walkingTime)
                    .minusSeconds(BUFFER_SEC_SECONDS)
                    .takeIf { it.isAfter(now) }
                    ?.let { OptimalDepartureTime(arrival, it) }
            }
            .minByOrNull { Duration.between(oldDepartureTime, it.routeDepartureTime).abs() }
    }

    private suspend fun saveUpdatedRoute(
        userRoute: UserRoute,
        route: LastRoute,
        busLeg: LastRouteLeg,
        optimalTime: OptimalDepartureTime
    ): UserRoute {
        val updatedRoute =
            updateRouteWithNewTimes(
                route,
                busLeg,
                optimalTime.busArrivalTime,
                optimalTime.routeDepartureTime
            )
        lastRouteAppender.append(updatedRoute)

        return userRouteManager.update(
            userRoute.updateDepartureTime(optimalTime.routeDepartureTime)
        )
    }

    private fun updateRouteWithNewTimes(
        route: LastRoute,
        busLeg: LastRouteLeg,
        newBusArrival: LocalDateTime,
        newRouteDeparture: LocalDateTime
    ): LastRoute {
        val updatedBusLeg = busLeg.copy(departureDateTime = newBusArrival.format(formatter))
        val updatedLegs = route.legs.map { if (it == busLeg) updatedBusLeg else it }

        return route.copy(
            departureDateTime = newRouteDeparture.format(formatter),
            legs = updatedLegs
        )
    }
}
