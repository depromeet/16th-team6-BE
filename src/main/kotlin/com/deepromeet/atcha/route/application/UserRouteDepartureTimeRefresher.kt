package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrivals
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

private const val DEPARTURE_THRESHOLD = 2
private const val MAX_SHIFT_LATER_SECONDS = 60L

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
    suspend fun refreshAll(): List<UserRoute> =
        userRouteManager.readAll().mapNotNull { userRoute ->
            refreshDepartureTime(userRoute)
        }

    suspend fun refreshDepartureTime(userRoute: UserRoute): UserRoute? {
        val route = lastRouteReader.read(userRoute.lastRouteId)

        // 1) 버스 구간 및 시간표 추출
        val firstBusLeg = extractFirstBusTransit(route) ?: return null
        val busInfo = firstBusLeg.requireBusInfo()

        // "20분 + 배차 간격" 윈도우 내에서만 갱신 시도 (현재 계획 기준)
        if (isNotRefreshTarget(userRoute.updatedDepartureTime, busInfo.timeTable.term)) {
            return null
        }

        // 2) 실시간 도착 정보 조회
        val arrivalInfo =
            busManager.getRealTimeArrival(
                firstBusLeg.resolveRouteName(),
                firstBusLeg.toBusStationMeta(),
                firstBusLeg.passStops!!
            )

        // 3) 도착 후보 시각 계산 및 최적 출발시간 결정 (늦어지는 후보는 배제)
        val optimalTime =
            calculateOptimalDepartureTime(
                arrivalInfo = arrivalInfo,
                busInfo = busInfo,
                route = route,
                userRoute = userRoute
            ) ?: return null

        return saveUpdatedRoute(userRoute, route, firstBusLeg, optimalTime)
    }

    private fun extractFirstBusTransit(route: LastRoute): LastRouteLeg? {
        val firstTransit = route.findFirstTransit()
        return if (firstTransit.isBus()) firstTransit else null
    }

    /** "배차간격 × 3" 이내가 아니면 갱신 금지 */
    private fun isNotRefreshTarget(
        plannedDeparture: LocalDateTime,
        busTerm: Int
    ): Boolean {
        val minutesLeft = Duration.between(LocalDateTime.now(), plannedDeparture).toMinutes()
        val refreshWindow = busTerm * 2
        return minutesLeft !in DEPARTURE_THRESHOLD until refreshWindow
    }

    private suspend fun calculateOptimalDepartureTime(
        arrivalInfo: BusRealTimeArrivals,
        busInfo: TransitInfo.BusInfo,
        route: LastRoute,
        userRoute: UserRoute
    ): OptimalDepartureTime? {
        val now = LocalDateTime.now()
        val walkingTime = route.calcWalkingTimeToFirstTransit()
        val baseDepartureTime = userRoute.baseDepartureTime

        val busPositions = busManager.getBusPositions(busInfo.busRouteInfo.route)

        val approachingBuses = busPositions.getApproachingBuses(busInfo.busStation)

        val candidates =
            arrivalInfo
                .createArrivalCandidatesWithPositions(busInfo, approachingBuses)
                .map { it.expectedArrivalTime!! }
                .mapNotNull { arrival ->
                    val newRouteDeparture =
                        arrival
                            .minusSeconds(walkingTime)

                    newRouteDeparture
                        .takeIf { it.isAfter(now) } // 지금 출발해도 도달 가능한가
                        ?.let { OptimalDepartureTime(arrival, it) }
                }
                // 60초 늦어지는거까지는 허용
                .filter { opt ->
                    val deltaSec = Duration.between(opt.routeDepartureTime, baseDepartureTime).seconds
                    deltaSec >= -MAX_SHIFT_LATER_SECONDS
                }

        if (candidates.isEmpty()) return null

        // 기존 계획과의 차이가 가장 작은 개선안을 선택
        return candidates.minByOrNull {
            Duration.between(baseDepartureTime, it.routeDepartureTime).abs()
        }
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

        val userRouteExpiresAt =
            updatedRoute
                .calculateArrivalTime()
                .plusHours(1)

        return userRouteManager.update(
            userRoute.updateDepartureTime(optimalTime.routeDepartureTime),
            userRouteExpiresAt
        )
    }

    private fun updateRouteWithNewTimes(
        route: LastRoute,
        busLeg: LastRouteLeg,
        newBusArrival: LocalDateTime,
        newRouteDeparture: LocalDateTime
    ): LastRoute {
        val updatedBusLeg = busLeg.copy(departureDateTime = newBusArrival)
        val updatedLegs = route.legs.map { if (it == busLeg) updatedBusLeg else it }

        return route.copy(
            departureDateTime = newRouteDeparture,
            legs = updatedLegs
        )
    }
}
