package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.api.response.LastRouteLeg
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class BusArrival(
    val busRoute: BusRoute,
    val busStationId: BusStationId,
    val stationName: String,
    val firstTime: LocalDateTime,
    val lastTime: LocalDateTime,
    val term: Int,
    val realTimeInfo: List<RealTimeBusArrival>
) {
    fun getNearestTime(
        time: LocalDateTime,
        timeDirection: TimeDirection
    ): LocalDateTime? {
        when (timeDirection) {
            TimeDirection.BEFORE -> {
                // 이전 버스를 찾는 경우
                if (time.isBefore(firstTime)) {
                    // 첫차 이전 시간에서는 이전 버스가 없음
                    return null
                }

                // 첫차부터 시작하여 버스 시간 계산
                var current = firstTime
                var prev: LocalDateTime? = null

                while (!current.isAfter(time)) {
                    prev = current
                    current = current.plusMinutes(term.toLong())
                    // 막차 시간을 넘어가면 중단
                    if (current.isAfter(lastTime)) {
                        break
                    }
                }

                return prev
            }

            TimeDirection.AFTER -> {
                // 이후 버스를 찾는 경우
                if (time.isAfter(lastTime)) {
                    // 막차 이후 시간에서는 이후 버스가 없음
                    return null
                }

                // 첫차 이전 시간이면 첫차가 다음 버스
                if (time.isBefore(firstTime)) {
                    return firstTime
                }

                // 막차 시간부터 역순으로 계산
                var temp = lastTime
                var candidate = lastTime

                while (temp.isAfter(time)) {
                    candidate = temp
                    temp = temp.minusMinutes(term.toLong())
                }

                return candidate
            }
        }
    }

    fun getSecondBus(): RealTimeBusArrival? = realTimeInfo.getOrNull(1)
}

data class RealTimeBusArrival(
    val vehicleId: String,
    val busStatus: BusStatus,
    val remainingTime: Int,
    val remainingStations: Int?,
    val isLast: Boolean?,
    val busCongestion: BusCongestion?,
    val remainingSeats: Int?
) {
    val expectedArrivalTime: LocalDateTime?
        get() =
            when (busStatus) {
                BusStatus.OPERATING, BusStatus.SOON ->
                    LocalDateTime.now()
                        .plusSeconds(remainingTime.toLong())
                else -> null
            }

    fun isTargetBus(targetBus: LastRouteLeg): Boolean {
        val targetBusDepartureTime = LocalDateTime.parse(targetBus.departureDateTime ?: return false)
        val diffMinutes = ChronoUnit.MINUTES.between(targetBusDepartureTime, expectedArrivalTime ?: return false)
        return kotlin.math.abs(diffMinutes) <= 5
    }
}
