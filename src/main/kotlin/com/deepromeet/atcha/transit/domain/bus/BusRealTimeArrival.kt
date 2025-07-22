package com.deepromeet.atcha.transit.domain.bus

import java.time.LocalDateTime

data class BusRealTimeArrival(
    val realTimeInfoList: List<BusRealTimeInfo>
) {
    /** 실시간 최대 2건 + 배차 기반 2건 → 총 4개의 도착 후보 시각 생성 */
    fun createArrivalCandidates(busTerm: Int): List<LocalDateTime> {
        val baseArrivals =
            realTimeInfoList
                .mapNotNull { it.expectedArrivalTime }
                .sorted()
                .toMutableList()

        val base = baseArrivals.lastOrNull() ?: return emptyList()
        repeat(2) { i ->
            baseArrivals += base.plusMinutes(busTerm.toLong() * (i + 1))
        }
        return baseArrivals
    }
}
