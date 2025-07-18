package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.event.UserRouteRefreshedEvent
import com.deepromeet.atcha.shared.domain.event.domain.EventBus
import org.springframework.stereotype.Service

@Service
class UserRouteRefreshService(
    private val userRouteDepartureTimeRefresher: UserRouteDepartureTimeRefresher,
    private val eventBus: EventBus
) {
    suspend fun refreshAllAndPublishEvents() {
        val refreshedRoutes = userRouteDepartureTimeRefresher.refreshAll()

        val events =
            refreshedRoutes.map { userRoute ->
                UserRouteRefreshedEvent(
                    aggregateId = userRoute.lastRouteId,
                    userRoute = userRoute
                )
            }

        eventBus.publishAll(events)
    }
}
