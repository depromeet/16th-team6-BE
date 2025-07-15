package com.deepromeet.atcha.notification.infrastructure.scheduler

import com.deepromeet.atcha.notification.domain.UserLastRouteStreamProducer
import com.deepromeet.atcha.transit.domain.route.LastRouteDepartureTimeRefresher
import kotlinx.coroutines.runBlocking
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotificationScheduler(
    private val lastRouteDepartureTimeRefresher: LastRouteDepartureTimeRefresher,
    private val userLastRouteStreamProducer: UserLastRouteStreamProducer
) {
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "refresh_push", lockAtMostFor = "PT2S", lockAtLeastFor = "PT2S")
    fun checkAndSendNotifications() =
        runBlocking {
            val updatedNotifications = lastRouteDepartureTimeRefresher.refreshAll()
            userLastRouteStreamProducer.produceAll(updatedNotifications)
        }
}
