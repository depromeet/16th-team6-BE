package com.deepromeet.atcha.transit.domain.bus

import com.deepromeet.atcha.transit.domain.TransitInfo
import java.time.Duration
import java.time.LocalDateTime

data class BusRealTimeArrivals(
    val realTimeInfoList: List<BusArrival>
) {
    /** BusPosition 정보를 활용한 더 정확한 도착 시간 계산 */
    fun getClosestArrivalsWithPositions(
        busInfo: TransitInfo.BusInfo,
        targetDepartureTime: LocalDateTime,
        approachingBuses: List<BusPosition>
    ): List<BusArrival>? {
        val arrivals = createArrivalCandidatesWithPositions(busInfo, approachingBuses)

        if (arrivals.isEmpty()) return null

        val targetIndex = getClosestArrivalIndex(arrivals, targetDepartureTime) ?: return null

        return createClosestTwoArrival(targetIndex, arrivals)
    }

    fun createArrivalCandidatesWithPositions(
        busInfo: TransitInfo.BusInfo,
        approachingBuses: List<BusPosition>
    ): List<BusArrival> {
        val realTimeBuses =
            realTimeInfoList
                .filter { it.expectedArrivalTime != null }
                .sortedBy { it.expectedArrivalTime }

        if (realTimeBuses.isEmpty()) return emptyList()

        val matchedBuses = mutableListOf<BusArrival>()
        val arrivingVehicleIds = realTimeBuses.map { it.vehicleId }

        realTimeBuses.forEach { realTimeBus -> matchedBuses += realTimeBus }

        val remainingPositions =
            approachingBuses
                .filter { it.vehicleId !in arrivingVehicleIds }
                .sortedByDescending { it.sectionOrder }

        // 마지막 실시간 도착시각 + n*term 로 추정 생성
        val term = busInfo.timeTable.term.toLong()
        val lastRealTime = realTimeBuses.last().expectedArrivalTime!!
        var nextIndex = 1

        for (pos in remainingPositions) {
            val nextArrivalTime = lastRealTime.plusMinutes(term * nextIndex)
            matchedBuses +=
                BusArrival.createEstimated(
                    vehicleId = pos.vehicleId,
                    estimatedArrivalTime = nextArrivalTime,
                    remainStations = busInfo.busRouteInfo.targetStation.order - pos.sectionOrder,
                    busCongestion = pos.busCongestion,
                    remainingSeats = pos.remainSeats
                )
            nextIndex++
        }

        // lastTime을 넘어가기 전까지 추가 버스 생성
        if (matchedBuses.last().expectedArrivalTime?.isBefore(busInfo.timeTable.lastTime) == true) {
            var currentArrivalTime = matchedBuses.last().expectedArrivalTime!!

            while (currentArrivalTime.isBefore(busInfo.timeTable.lastTime)) {
                currentArrivalTime = currentArrivalTime.plusMinutes(term)
                if (!currentArrivalTime.isAfter(busInfo.timeTable.lastTime)) {
                    matchedBuses +=
                        BusArrival.createEstimated(
                            vehicleId = "ESTIMATED_$nextIndex",
                            estimatedArrivalTime = currentArrivalTime,
                            remainStations = null,
                            busCongestion = null,
                            remainingSeats = null
                        )
                    nextIndex++
                }
            }
        }

        return matchedBuses.sortedBy { it.expectedArrivalTime }
    }

    private fun getClosestArrivalIndex(
        arrivals: List<BusArrival>,
        targetDepartureTime: LocalDateTime
    ): Int? =
        arrivals.withIndex().minByOrNull { (_, arrival) ->
            Duration.between(targetDepartureTime, arrival.expectedArrivalTime!!).abs()
        }?.index

    private fun createClosestTwoArrival(
        targetIndex: Int,
        arrivals: List<BusArrival>
    ): List<BusArrival> =
        buildList {
            add(arrivals[targetIndex])
            if (targetIndex + 1 < arrivals.size) {
                add(arrivals[targetIndex + 1])
            }
        }
}
