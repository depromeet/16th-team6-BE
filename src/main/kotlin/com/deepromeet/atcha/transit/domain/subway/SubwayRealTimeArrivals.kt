package com.deepromeet.atcha.transit.domain.subway

import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import java.time.Duration
import java.time.LocalDateTime

class SubwayRealTimeArrivals(
    val realTimeInfoList: List<SubwayArrival>
) {

    /** BusPosition 정보를 활용한 더 정확한 도착 시간 계산 */
    fun getClosestSubwayArrivalsWithPositions(
        subwayInfo: TransitInfo.SubwayInfo,
        targetDepartureTime: LocalDateTime,
        approachingBuses: List<BusPosition>
    ): List<SubwayArrival>? {
        val arrivals = createArrivalSubwayCandidatesWithPositions(subwayInfo, approachingBuses)

        if (arrivals.isEmpty()) return null

        val targetIndex: Int = getClosestArrivalIndex(arrivals, targetDepartureTime) ?: return null

        return createClosestTwoArrival(targetIndex, arrivals)
    }

    private fun getClosestArrivalIndex(
        arrivals: List<SubwayArrival>,
        targetDepartureTime: LocalDateTime
    ): Int? =
        arrivals.withIndex().minByOrNull { (_, arrival) ->
            Duration.between(targetDepartureTime, arrival.expectedArrivalTime!!).abs()
        }?.index

    fun createArrivalSubwayCandidatesWithPositions(
        subwayInfo: TransitInfo.SubwayInfo,
        approachingBuses: List<BusPosition>
    ): List<SubwayArrival> {
        val realTimeBuses =
            realTimeInfoList
                .filter { it.expectedArrivalTime != null }
                .sortedBy { it.expectedArrivalTime }

        if (realTimeBuses.isEmpty()) return emptyList()

        val matchedBuses = mutableListOf<SubwayArrival>()
        val arrivingVehicleIds = realTimeBuses.map { it.vehicleId }

        realTimeBuses.forEach { realTimeBus -> matchedBuses += realTimeBus }

        val remainingPositions =
            approachingBuses
                .filter { it.vehicleId !in arrivingVehicleIds }
                .sortedByDescending { it.sectionOrder }

        // 마지막 실시간 도착시각 + n*term 로 추정 생성
        val term = subwayInfo.timeTable.term.toLong()
        val lastRealTime = realTimeBuses.last().expectedArrivalTime!!
        var nextIndex = 1

        for (pos in remainingPositions) {
            val nextArrivalTime = lastRealTime.plusMinutes(term * nextIndex)
            matchedBuses +=
                SubwayArrival.createEstimated(
                    vehicleId = pos.vehicleId,
                    estimatedArrivalTime = nextArrivalTime,
                    remainStations = subwayInfo.busRouteInfo.targetStation.order - pos.sectionOrder,
                    busCongestion = pos.busCongestion,
                    remainingSeats = pos.remainSeats
                )
            nextIndex++
        }

        // lastTime을 넘어가기 전까지 추가 버스 생성
        if (matchedBuses.last().expectedArrivalTime?.isBefore(subwayInfo.timeTable.lastTime) == true) {
            var currentArrivalTime = matchedBuses.last().expectedArrivalTime!!

            while (currentArrivalTime.isBefore(subwayInfo.timeTable.lastTime)) {
                currentArrivalTime = currentArrivalTime.plusMinutes(term)
                if (!currentArrivalTime.isAfter(subwayInfo.timeTable.lastTime)) {
                    matchedBuses +=
                        SubwayArrival.createEstimated(
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

    private fun createClosestTwoArrival(
        targetIndex: Int,
        arrivals: List<SubwayArrival>
    ): List<SubwayArrival> =
        buildList {
            add(arrivals[targetIndex])
            if (targetIndex + 1 < arrivals.size) {
                add(arrivals[targetIndex + 1])
            }
        }
}
