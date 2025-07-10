package com.deepromeet.atcha.notification.infrastructure.scheduler

import com.deepromeet.atcha.notification.domatin.UserLastRouteStreamProducer
import com.deepromeet.atcha.transit.domain.RouteDepartureTimeRefresher
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotificationScheduler(
    private val routeDepartureTimeRefresher: RouteDepartureTimeRefresher,
    private val userLastRouteStreamProducer: UserLastRouteStreamProducer
) {
    @Scheduled(cron = "0 * * * * ?")
    fun checkAndSendNotifications() =
        runBlocking {
            val updatedNotifications = routeDepartureTimeRefresher.refreshAll()
            userLastRouteStreamProducer.produceAll(updatedNotifications)
        }
}
