package com.deepromeet.atcha.userroute.application

import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusManager
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeInfo
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import com.deepromeet.atcha.transit.domain.route.LastRoute
import com.deepromeet.atcha.transit.domain.route.LastRouteAppender
import com.deepromeet.atcha.transit.domain.route.LastRouteLeg
import com.deepromeet.atcha.transit.domain.route.LastRouteReader
import com.deepromeet.atcha.userroute.domain.UserRoute
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    suspend fun refreshAll(): List<UserRoute> = userRouteManager.readAll().mapNotNull { refreshDepartureTime(it) }

    suspend fun refreshDepartureTime(userRoute: UserRoute): UserRoute? {
        val oldDeparture = LocalDateTime.parse(userRoute.departureTime, formatter)
        val route = lastRouteReader.read(userRoute.lastRouteId)

        // 1) 버스 구간 및 시간표 추출
        val firstBusLeg = extractFirstBusTransit(route) ?: return null
        val timeTable = extractBusTimeTable(firstBusLeg) ?: return null

//        if (isNotRefreshTarget(oldDeparture, timeTable.term)) return null

        // 2) 실시간 도착 정보 조회
        val arrivalInfos = getBusRealTimeInfo(firstBusLeg) ?: return null

        // 3) 도착 후보 시각 계산 및 최적 출발시간 결정
        val optimalTime =
            calculateOptimalDepartureTime(
                arrivalInfos,
                timeTable,
                firstBusLeg,
                route
            ) ?: return null

        // 4) 갱신된 route 및 알림 저장
        return saveUpdatedRoute(userRoute, route, firstBusLeg, optimalTime)
    }

    private fun extractFirstBusTransit(route: LastRoute): LastRouteLeg? {
        val firstTransit = route.findFirstTransit()
        return if (firstTransit.isBus()) firstTransit else null
    }

    private fun extractBusTimeTable(transit: LastRouteLeg): BusTimeTable? {
        return (transit.transitInfo as? TransitInfo.BusInfo)?.timeTable
    }

    private suspend fun getBusRealTimeInfo(firstTransit: LastRouteLeg): List<BusRealTimeInfo>? {
        return busManager.getRealTimeArrival(
            firstTransit.resolveRouteName(),
            firstTransit.toBusStationMeta(),
            firstTransit.passStopList!!
        ).realTimeInfoList.ifEmpty { null }
    }

    private fun calculateOptimalDepartureTime(
        arrivalInfos: List<BusRealTimeInfo>,
        timeTable: BusTimeTable,
        firstBusLeg: LastRouteLeg,
        route: LastRoute
    ): OptimalDepartureTime? {
        // 도착 후보 시각 생성
        val candidateArrivals =
            createArrivalCandidates(arrivalInfos, timeTable.term)
                .filter { it.isBefore(timeTable.lastTime) }
                .toSet()

        // 실행 가능한 출발시간 계산
        val now = LocalDateTime.now()
        val walkingTime = route.calcWalkingTimeBeforeLeg(firstBusLeg)

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
        return minutesLeft !in 3 until (FIXED_REFRESH_MINUTES + busTerm)
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
