package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationManager
import com.deepromeet.atcha.notification.domatin.UserNotificationReader
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val MIN_DIFF_MINUTES = 3
private const val MIN_REFRESH_MINUTES = 20

@Component
class RouteDepartureTimeRefresher(
    private val userNotificationReader: UserNotificationReader,
    private val userNotificationManager: UserNotificationManager,
    private val lastRouteAppender: LastRouteAppender,
    private val busManager: BusManager,
    private val lastRouteReader: LastRouteReader
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun refreshAll(): List<UserNotification> {
        return userNotificationReader.findAll().mapNotNull { userNotification ->
            refreshDepartureTime(userNotification)
        }
    }

    suspend fun refreshDepartureTime(notification: UserNotification): UserNotification? {
        val oldDepartureTime = LocalDateTime.parse(notification.updatedDepartureTime, dateTimeFormatter)

        val route = lastRouteReader.read(notification.lastRouteId)

        // 2) 첫 번째 버스 구간 찾기
        val firstBusLeg = route.findFirstBus()

        // 버스 시간표 가져오기
        val timeTable = (firstBusLeg.transitInfo as? TransitInfo.BusInfo)?.timeTable ?: return null

        if (isNotRefreshTarget(oldDepartureTime, timeTable.term)) return null

        // 현재 버스 예상 출발 시간
        val originalBusDepartureTime = LocalDateTime.parse(firstBusLeg.departureDateTime!!, dateTimeFormatter)

        // 3) 버스 정보 조회
        val busArrival =
            runCatching {
                busManager.getRealTimeArrival(
                    firstBusLeg.resolveRouteName(),
                    firstBusLeg.toBusStationMeta(),
                    firstBusLeg.passStopList!!
                )
            }.getOrElse { return null }

        // 4) 실시간 정보를 활용해 최대 4개 후보 시간 생성
        val realTimeInfos = busArrival.realTimeInfoList
        if (realTimeInfos.isEmpty()) return null

        // 예상 도착 시간이 존재하는 최대 2개 후보 생성
        val candidateTimes =
            realTimeInfos
                .filter { it.expectedArrivalTime != null }
                .map { it.expectedArrivalTime }
                .take(2).toMutableSet()

        // 여기서 그냥 후보 2개를 생성하는 것이 아니라,
        // 실시간 버스 위치 정보를 활용하여 현재 실시간 정보가 있는 버스 이외에 더 몇개의 버스가 있는지 확인하여
        // 그 버스들의 수만큼 후보 시간을 생성

        if (candidateTimes.isEmpty()) return null

        // 실시간 정보 중 가장 늦은 도착 시각을 기준으로 후보 시간 생성
        val baseArrival =
            realTimeInfos
                .maxWith(compareBy { it.expectedArrivalTime })
                .expectedArrivalTime ?: return null

        // 가장 늦은 도착 시각을 기준으로 배차간격만큼 후보 시간 2개 생성
        repeat(2) { i ->
            candidateTimes += baseArrival.plusMinutes(timeTable.term.toLong() * (i + 1))
        }

        // 5) 기존 버스 출발 시각과 가장 가까운 도착 시각 선택
        val chosenArrivalTime =
            candidateTimes
                .filter { it!!.isBefore(timeTable.lastTime) }
                .minBy { candidate -> Duration.between(originalBusDepartureTime, candidate).abs() }
                ?: return null

        // 버스 구간 출발 시간을 chosenArrivalTime으로 재설정
        val updatedBusLeg =
            firstBusLeg.copy(
                departureDateTime = chosenArrivalTime.format(dateTimeFormatter)
            )

        // legs 전체에서 firstBusLeg만 업데이트
        val updatedLegs =
            route.legs.map {
                if (it == firstBusLeg) updatedBusLeg else it
            }

        // 6) 집에서 출발해야 하는 시간을 다시 계산
        val walkSec = getWalkTimeBeforeThisLeg(route, firstBusLeg)
        val bufferSec = 2 * 60 // 2분
        val newDepartureTime =
            chosenArrivalTime
                .minusSeconds(walkSec.toLong())
                .minusSeconds(bufferSec.toLong())

        // route 업데이트 & Redis 저장
        val updatedRoute =
            route.copy(
                departureDateTime = newDepartureTime.format(dateTimeFormatter),
                legs = updatedLegs
            )

        lastRouteAppender.append(updatedRoute)
        return userNotificationManager.saveUserNotification(notification.updateDepartureTime(newDepartureTime))
    }

    private fun isNotRefreshTarget(
        oldDepartureTime: LocalDateTime,
        busTerm: Int
    ): Boolean {
        // 출발 시간이 현재 시간으로부터 배차 간격 이내에 있는 경우에만 업데이트
        // 시간 기준 : 고정 20분 + 배차간격
        val now = LocalDateTime.now()
        val minutesUntilDeparture = Duration.between(now, oldDepartureTime).toMinutes()
        return minutesUntilDeparture !in 0 until (MIN_REFRESH_MINUTES + busTerm)
    }

    private fun getWalkTimeBeforeThisLeg(
        route: LastRoute,
        busLeg: LastRouteLeg
    ): Int {
        var totalWalkTime = 0
        for (leg in route.legs) {
            if (leg == busLeg) break
            if (leg.mode == "WALK") {
                totalWalkTime += leg.sectionTime
            }
        }
        return totalWalkTime
    }
}
