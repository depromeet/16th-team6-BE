package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteLeg
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class LastRouteUpdater(
    private val lastRouteCache: LastRouteCache
) {
    /**
     * 실시간 정보로 첫 번째 버스의 출발 시간을 업데이트하고 캐시에 저장
     */
    fun updateFirstBusTime(
        lastRoute: LastRoute,
        firstBus: LastRouteLeg,
        newArrivalTime: LocalDateTime
    ) {
        val updatedLegs =
            lastRoute.legs.map { leg ->
                if (leg == firstBus) {
                    leg.copy(departureDateTime = newArrivalTime)
                } else {
                    leg
                }
            }

        val updatedRoute = lastRoute.copy(legs = updatedLegs)

        lastRouteCache.cache(updatedRoute)
    }
}
