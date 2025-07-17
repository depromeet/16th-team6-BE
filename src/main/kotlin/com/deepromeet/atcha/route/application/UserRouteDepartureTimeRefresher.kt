package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeInfo
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
            val oldDeparture = LocalDateTime.parse(userRoute.departureTime, formatter)
            val route = lastRouteReader.read(userRoute.lastRouteId)

            val firstBusLeg = extractFirstBusTransit(route) ?: return@mapNotNull null
            val timeTable = firstBusLeg.busInfo?.timeTable ?: return@mapNotNull null

            if (isNotRefreshTarget(oldDeparture, timeTable.term)) {
                return@mapNotNull null
            }

            refreshDepartureTime(userRoute)
        }

    suspend fun refreshDepartureTime(userRoute: UserRoute): UserRoute? {
        val route = lastRouteReader.read(userRoute.lastRouteId)

        // 1) 버스 구간 및 시간표 추출
        val firstBusLeg = extractFirstBusTransit(route) ?: return null
        val timeTable = firstBusLeg.busInfo?.timeTable ?: return null

        // 2) 실시간 도착 정보 조회
        val arrivalInfos = getBusRealTimeInfo(firstBusLeg) ?: return null

        // 3) 도착 후보 시각 계산 및 최적 출발시간 결정
        val optimalTime =
            calculateOptimalDepartureTime(
                arrivalInfos,
                timeTable,
                route
            ) ?: return null

        // 4) 갱신된 route 및 알림 저장
        return saveUpdatedRoute(userRoute, route, firstBusLeg, optimalTime)
    }

    private fun extractFirstBusTransit(route: LastRoute): LastRouteLeg? {
        val firstTransit = route.findFirstTransit()
        return if (firstTransit.isBus()) firstTransit else null
    }

    private suspend fun getBusRealTimeInfo(firstTransit: LastRouteLeg): List<BusRealTimeInfo>? {
        return busManager.getRealTimeArrival(
            firstTransit.resolveRouteName(),
            firstTransit.toBusStationMeta(),
            firstTransit.passStops!!
        ).realTimeInfoList.ifEmpty { null }
    }

    private fun calculateOptimalDepartureTime(
        arrivalInfos: List<BusRealTimeInfo>,
        timeTable: BusTimeTable,
        route: LastRoute
    ): OptimalDepartureTime? {
        // 도착 후보 시각 생성
        val candidateArrivals =
            createArrivalCandidates(arrivalInfos, timeTable.term)
                .filter { it.isBefore(timeTable.lastTime) }
                .toSet()

        // 실행 가능한 출발시간 계산
        val now = LocalDateTime.now()
        val walkingTime = route.calcWalkingTimeToFirstTransit()

        val feasible =
            candidateArrivals.mapNotNull { arrival ->
                val departure =
                    arrival
                        .minusSeconds(walkingTime)
                        .minusSeconds(BUFFER_SEC_SECONDS)
                if (departure.isAfter(now)) {
                    OptimalDepartureTime(
                        arrival,
                        departure
                    )
                } else {
                    null
                }
            }

        return feasible.minByOrNull { it.routeDepartureTime }
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

    /** "20분 + 배차 간격" 이내가 아니면 갱신 금지 */
    private fun isNotRefreshTarget(
        oldDeparture: LocalDateTime,
        busTerm: Int
    ): Boolean {
        val minutesLeft = Duration.between(LocalDateTime.now(), oldDeparture).toMinutes()
        return minutesLeft !in BUS_ARRIVAL_THRESHOLD_MINUTES until (FIXED_REFRESH_MINUTES + busTerm)
    }

    /** 실시간 최대 2건 + 배차 기반 2건 → 총 4개의 도착 후보 시각 생성 */
    private fun createArrivalCandidates(
        infos: List<BusRealTimeInfo>,
        busTerm: Int
    ): List<LocalDateTime> {
        val baseArrivals =
            infos
                .mapNotNull { it.expectedArrivalTime }
                .sorted()
                .takeLast(1)
                .toMutableList()

        val base = baseArrivals.firstOrNull() ?: return emptyList()
        repeat(2) { i -> baseArrivals += base.plusMinutes(busTerm.toLong() * (i + 1)) }

        return baseArrivals
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
