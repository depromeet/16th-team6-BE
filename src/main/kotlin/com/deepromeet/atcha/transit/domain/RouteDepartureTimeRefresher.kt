package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.notification.domain.UserLastRoute
import com.deepromeet.atcha.notification.domain.UserLastRouteManager
import com.deepromeet.atcha.notification.domain.UserLastRouteReader
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val FIXED_REFRESH_MINUTES = 20
private const val BUFFER_SEC_SECONDS = 2 * 60

@Component
class RouteDepartureTimeRefresher(
    private val userLastRouteReader: UserLastRouteReader,
    private val userLastRouteManager: UserLastRouteManager,
    private val lastRouteAppender: LastRouteAppender,
    private val busManager: BusManager,
    private val lastRouteReader: LastRouteReader
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun refreshAll(): List<UserLastRoute> =
        userLastRouteReader.findAll().mapNotNull { refreshDepartureTime(it) }

    suspend fun refreshDepartureTime(notification: UserLastRoute): UserLastRoute? {
        val oldDeparture = LocalDateTime.parse(notification.departureTime, formatter)
        val route = lastRouteReader.read(notification.lastRouteId)

        // 1) 버스 구간 및 시간표 추출
        val firstTransit = extractFirstBusTransit(route) ?: return null
        val timeTable = extractBusTimeTable(firstTransit) ?: return null

        if (isNotRefreshTarget(oldDeparture, timeTable.term)) return null

        // 2) 실시간 도착 정보 조회
        val arrivalInfos = getBusRealTimeInfo(firstTransit) ?: return null

        // 3) 도착 후보 시각 계산 및 최적 출발시간 결정
        val optimalTimes =
            calculateOptimalDepartureTimes(
                arrivalInfos,
                timeTable,
                firstTransit,
                route
            ) ?: return null

        // 4) 갱신된 route 및 알림 저장
        return saveUpdatedRoute(notification, route, firstTransit, optimalTimes)
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

    private fun calculateOptimalDepartureTimes(
        arrivalInfos: List<BusRealTimeInfo>,
        timeTable: BusTimeTable,
        firstTransit: LastRouteLeg,
        route: LastRoute
    ): Pair<LocalDateTime, LocalDateTime>? {
        // 도착 후보 시각 생성
        val candidateArrivals =
            createArrivalCandidates(arrivalInfos, timeTable.term)
                .filter { it.isBefore(timeTable.lastTime) }
                .toSet()

        // 실행 가능한 출발시간 계산
        val now = LocalDateTime.now()
        val walkSec = calcWalkSecBefore(firstTransit, route)

        val feasible =
            candidateArrivals.mapNotNull { arrival ->
                val dep =
                    arrival
                        .minusSeconds(walkSec.toLong())
                        .minusSeconds(BUFFER_SEC_SECONDS.toLong())
                if (dep.isAfter(now)) arrival to dep else null
            }

        return feasible.minByOrNull { it.second }
    }

    private suspend fun saveUpdatedRoute(
        notification: UserLastRoute,
        route: LastRoute,
        busLeg: LastRouteLeg,
        optimalTimes: Pair<LocalDateTime, LocalDateTime>
    ): UserLastRoute {
        val (chosenArrival, newDeparture) = optimalTimes

        val updatedRoute = updateRouteWithNewTimes(route, busLeg, chosenArrival, newDeparture)
        lastRouteAppender.append(updatedRoute)

        return userLastRouteManager.saveUserNotification(
            notification.updateDepartureTime(newDeparture)
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

    private fun calcWalkSecBefore(
        targetLeg: LastRouteLeg,
        route: LastRoute
    ): Int =
        route.legs
            .takeWhile { it != targetLeg }
            .filter { it.mode == "WALK" }
            .sumOf { it.sectionTime }

    private fun updateRouteWithNewTimes(
        route: LastRoute,
        busLeg: LastRouteLeg,
        newBusDeparture: LocalDateTime,
        newRouteDeparture: LocalDateTime
    ): LastRoute {
        val updatedBusLeg = busLeg.copy(departureDateTime = newBusDeparture.format(formatter))
        val updatedLegs = route.legs.map { if (it == busLeg) updatedBusLeg else it }

        return route.copy(
            departureDateTime = newRouteDeparture.format(formatter),
            legs = updatedLegs
        )
    }
}
