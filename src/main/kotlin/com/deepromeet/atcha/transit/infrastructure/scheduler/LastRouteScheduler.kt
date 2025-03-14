package com.deepromeet.atcha.transit.infrastructure.scheduler

import com.deepromeet.atcha.transit.domain.RouteDepartureTimeRefresher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class LastRouteScheduler(
    private val routeDepartureTimeRefresher: RouteDepartureTimeRefresher
) {
    @Scheduled(fixedRate = 60000)
    fun refreshDepartureTime() {
        routeDepartureTimeRefresher.refresh()
    }
}
