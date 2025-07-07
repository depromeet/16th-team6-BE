package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationManager
import com.deepromeet.atcha.notification.domatin.UserNotificationReader
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val FIXED_REFRESH_MINUTES = 20
private const val BUFFER_SEC_SECONDS = 2 * 60

@Component
class RouteDepartureTimeRefresher(
    private val userNotificationReader: UserNotificationReader,
    private val userNotificationManager: UserNotificationManager,
    private val lastRouteAppender: LastRouteAppender,
    private val busManager: BusManager,
    private val lastRouteReader: LastRouteReader
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun refreshAll(): List<UserNotification> =
        userNotificationReader.findAll().mapNotNull { refreshDepartureTime(it) }

    suspend fun refreshDepartureTime(notification: UserNotification): UserNotification? {
        val oldDeparture = LocalDateTime.parse(notification.updatedDepartureTime, formatter)
        val route = lastRouteReader.read(notification.lastRouteId)

        // 1) 버스 구간 및 시간표
        val firstBusLeg = route.findFirstBus()
        val timeTable = (firstBusLeg.transitInfo as? TransitInfo.BusInfo)?.timeTable ?: return null

        if (isNotRefreshTarget(oldDeparture, timeTable.term)) return null

        // 2) 실시간 도착 정보
        val arrivalInfos =
            busManager.getRealTimeArrival(
                firstBusLeg.resolveRouteName(),
                firstBusLeg.toBusStationMeta(),
                firstBusLeg.passStopList!!
            ).realTimeInfoList.ifEmpty { return null }

        // 3) 도착 후보 시각 계산
        val candidateArrivals =
            createArrivalCandidates(arrivalInfos, timeTable.term)
                .filter { it.isBefore(timeTable.lastTime) }
                .toSet()

        val now = LocalDateTime.now()
        val walkSec = calcWalkSecBefore(firstBusLeg, route)
        val feasible =
            candidateArrivals.mapNotNull { arrival ->
                val dep =
                    arrival
                        .minusSeconds(walkSec.toLong())
                        .minusSeconds(BUFFER_SEC_SECONDS.toLong())
                if (dep.isAfter(now)) arrival to dep else null
            }

        val (chosenArrival, newDeparture) = feasible.minByOrNull { it.second } ?: return null

        // 5) 갱신된 route 및 알림 저장
        val updatedRoute = route.updateWithNewTimes(firstBusLeg, chosenArrival, newDeparture)
        lastRouteAppender.append(updatedRoute)

        return userNotificationManager.saveUserNotification(
            notification.updateDepartureTime(newDeparture)
        )
    }

    /** “20분 + 배차 간격” 이내가 아니면 갱신 금지 */
    private fun isNotRefreshTarget(
        oldDeparture: LocalDateTime,
        busTerm: Int
    ): Boolean {
        val minutesLeft = Duration.between(LocalDateTime.now(), oldDeparture).toMinutes()
        return minutesLeft !in 0 until (FIXED_REFRESH_MINUTES + busTerm)
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

    private fun LastRoute.updateWithNewTimes(
        busLeg: LastRouteLeg,
        newBusDeparture: LocalDateTime,
        newRouteDeparture: LocalDateTime
    ): LastRoute {
        val updatedBusLeg = busLeg.copy(departureDateTime = newBusDeparture.format(formatter))
        val updatedLegs = legs.map { if (it == busLeg) updatedBusLeg else it }

        return copy(
            departureDateTime = newRouteDeparture.format(formatter),
            legs = updatedLegs
        )
    }
}
