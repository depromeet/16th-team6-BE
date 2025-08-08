package com.deepromeet.atcha.transit.domain.bus

import java.time.Duration
import java.time.LocalDateTime

data class BusRealTimeArrival(
    val realTimeInfoList: List<BusRealTimeInfo>
) {
    /** 실시간 정보 + 배차 기반으로 총 접근 중인 버스 수만큼 후보 시각 생성 */
    fun createArrivalCandidates(
        timeTable: BusTimeTable,
        approachingBusCount: Int
    ): List<LocalDateTime> {
        val baseArrivals =
            realTimeInfoList
                .mapNotNull { it.expectedArrivalTime }
                .sorted()
                .toMutableList()

        val base = baseArrivals.lastOrNull() ?: return emptyList()

        var nextIndex = 1
        while (baseArrivals.size < approachingBusCount) {
            val nextArrival = base.plusMinutes(timeTable.term.toLong() * nextIndex)
            if (nextArrival.isBefore(timeTable.lastTime)) {
                baseArrivals += nextArrival
                nextIndex++
            } else {
                break
            }
        }

        return baseArrivals
    }

    fun getClosestArrival(
        timeTable: BusTimeTable,
        targetDepartureTime: LocalDateTime,
        approachingBusCount: Int
    ): LocalDateTime? {
        return createArrivalCandidates(timeTable, approachingBusCount)
            .minByOrNull { Duration.between(targetDepartureTime, it).abs() }
    }
}
