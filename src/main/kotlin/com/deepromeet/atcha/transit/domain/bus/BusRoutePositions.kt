package com.deepromeet.atcha.transit.domain.bus

import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.AVERAGE_MINUTE_PER_STATION
import java.time.Duration
import java.time.LocalDateTime

data class BusRoutePositions(
    val routeStations: BusRouteStationList,
    val busPositions: List<BusPosition>
) {
    fun findTargetBus(
        busStation: BusStation,
        departureDateTime: String,
        term: Int
    ): BusPosition? {
        val target = routeStations.getTargetStationById(busStation.id) ?: return null
        val depTime = LocalDateTime.parse(departureDateTime)

        return busPositions.firstOrNull { pos -> // 일치하는 첫 버스 반환
            val remainStations = target.order - pos.sectionOrder
            if (remainStations < 0) return@firstOrNull false

            val nearGarage =
                when (target.resolveDirection()) {
                    BusDirection.UP -> pos.sectionOrder < 5
                    BusDirection.DOWN -> pos.sectionOrder in (target.turnPoint!! + 1)..(target.turnPoint + 5)
                }
            if (!nearGarage) return@firstOrNull false

            val etaMinutes = remainStations * AVERAGE_MINUTE_PER_STATION
            val arriveAt = LocalDateTime.now().plusMinutes(etaMinutes.toLong())
            val diffMin = Duration.between(depTime, arriveAt).toMinutes()

            diffMin in 0..term
        }
    }
}
