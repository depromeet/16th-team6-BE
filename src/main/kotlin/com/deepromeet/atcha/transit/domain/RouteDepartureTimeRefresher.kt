package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.api.response.LastRouteLeg
import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class RouteDepartureTimeRefresher(
    private val lastRouteCache: LastRouteCache,
    private val lastRouteAppender: LastRouteAppender,
    private val busManager: BusManager
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun refresh() {
        lastRouteCache.processRoutes { route ->
            refreshDepartureTime(route)
        }
    }

    private fun refreshDepartureTime(route: LastRoutesResponse) {
        val oldDepartureTime = LocalDateTime.parse(route.departureDateTime, dateTimeFormatter)

        // 1) 20분 이내 체크
        val now = LocalDateTime.now()
        val minutesUntilDeparture = Duration.between(now, oldDepartureTime).toMinutes()
        if (minutesUntilDeparture > 20 || minutesUntilDeparture < 0) {
            return
        }

        // 2) 첫 번째 버스 구간 찾기
        val firstBusLeg = route.legs.firstOrNull { it.mode == "BUS" } ?: return
        // 현재 저장된 버스 출발 시간 (문자열 → LocalDateTime)
        val originalBusDepartureTime = LocalDateTime.parse(firstBusLeg.departureDateTime!!, dateTimeFormatter)

        val routeName = firstBusLeg.route!!.split(":")[1]

        // 3) 버스 정보 조회
        val busStationMeta =
            BusStationMeta(
                name = firstBusLeg.start.name,
                coordinate =
                    Coordinate(
                        firstBusLeg.start.lat,
                        firstBusLeg.start.lon
                    )
            )
        val busArrival = busManager.getArrivalInfo(routeName, busStationMeta) ?: return

        // 4) 실시간 정보를 활용해 최대 4개 후보 시간 생성
        val realTimeInfos = busArrival.realTimeInfo
        if (realTimeInfos.isEmpty()) return

        val candidateTimes = mutableListOf<LocalDateTime>()
        realTimeInfos.forEach { rt ->
            val arrivalTime = rt.expectedArrivalTime ?: return@forEach
            candidateTimes += arrivalTime
            candidateTimes += arrivalTime.plusMinutes(busArrival.term.toLong())
        }

        // 5) 기존 버스 출발 시각과 가장 가까운 도착 시각 선택
        val chosenArrivalTime =
            candidateTimes.minByOrNull { candidate ->
                Duration.between(originalBusDepartureTime, candidate).abs()
            } ?: return

        // (a) 버스 구간 출발 시간을 chosenArrivalTime으로 재설정
        val updatedBusLeg =
            firstBusLeg.copy(
                departureDateTime = chosenArrivalTime.format(dateTimeFormatter)
            )

        // (b) legs 전체에서 firstBusLeg만 업데이트
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

        // 7) route 업데이트 & Redis 저장
        val updatedRoute =
            route.copy(
                departureDateTime = newDepartureTime.format(dateTimeFormatter),
                legs = updatedLegs
            )

        lastRouteAppender.append(updatedRoute)
    }

    private fun getWalkTimeBeforeThisLeg(
        route: LastRoutesResponse,
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
