package com.deepromeet.atcha.transit.domain.bus

import java.time.Duration
import java.time.LocalDateTime

data class BusRealTimeArrival(
    val realTimeInfoList: List<BusRealTimeInfo>
) {
    /** BusPosition 정보를 활용한 더 정확한 후보 생성 */
    fun createArrivalCandidatesWithPositions(
        timeTable: BusTimeTable,
        approachingBuses: List<BusPosition>
    ): List<BusRealTimeInfo> {
        val realTimeBuses =
            realTimeInfoList
                .filter { it.expectedArrivalTime != null }
                .sortedBy { it.expectedArrivalTime }

        if (realTimeBuses.isEmpty()) return emptyList()

        val matchedBuses = mutableListOf<BusRealTimeInfo>()
        val usedVehicleIds = mutableSetOf<String>()

        realTimeBuses.forEach { rt ->
            val pos = approachingBuses.find { it.vehicleId == rt.vehicleId }
            if (pos != null) {
                matchedBuses +=
                    rt.copy(
                        busCongestion = pos.busCongestion,
                        remainingSeats = pos.remainSeats
                    )
                usedVehicleIds += rt.vehicleId
            } else {
                matchedBuses += rt
            }
        }

        val remainingPositions =
            approachingBuses
                .filter { it.vehicleId !in usedVehicleIds }
                .sortedByDescending { it.sectionOrder }

        // 마지막 실시간 도착시각 + n*term 로 추정 생성
        val term = timeTable.term.toLong()
        val lastRealTime = realTimeBuses.last().expectedArrivalTime!!
        var nextIndex = 1

        for (pos in remainingPositions) {
            val nextArrivalTime = lastRealTime.plusMinutes(term * nextIndex)

            if (!nextArrivalTime.isAfter(timeTable.lastTime)) { // <= lastTime 이내만
                matchedBuses +=
                    BusRealTimeInfo.createEstimated(
                        vehicleId = pos.vehicleId,
                        estimatedArrivalTime = nextArrivalTime,
                        busCongestion = pos.busCongestion,
                        remainingSeats = pos.remainSeats
                    )
                nextIndex++
            } else {
                break
            }
        }

        return matchedBuses.sortedBy { it.expectedArrivalTime }
    }

    /** BusPosition 정보를 활용한 더 정확한 도착 시간 계산 */
    fun getClosestArrivalWithPositions(
        timeTable: BusTimeTable,
        targetDepartureTime: LocalDateTime,
        approachingBuses: List<BusPosition>
    ): BusRealTimeInfo? {
        return createArrivalCandidatesWithPositions(timeTable, approachingBuses)
            .minByOrNull {
                it.expectedArrivalTime!!.let { arrivalTime ->
                    Duration.between(targetDepartureTime, arrivalTime).abs()
                }
            }
    }
}
