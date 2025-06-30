package com.deepromeet.atcha.notification.infrastructure.scheduler

import com.deepromeet.atcha.transit.domain.DepartureDelayNotifier
import com.deepromeet.atcha.transit.domain.RouteDepartureTimeRefresher
import com.deepromeet.atcha.transit.domain.ScheduledNotificationPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class NotificationScheduler(
    private val routeDepartureTimeRefresher: RouteDepartureTimeRefresher,
    private val departureDelayNotifier: DepartureDelayNotifier,
    private val scheduledNotificationPublisher: ScheduledNotificationPublisher
) {
    @Scheduled(cron = "0 * 0-3,21-23 * * ?")
    fun checkAndSendNotifications() {
        // 예상 출발 시간 갱신
        val updatedNotifications = routeDepartureTimeRefresher.refresh()

        // 지연 알림 전송
        updatedNotifications.forEach {
            departureDelayNotifier.notifyIfDelayed(it)
        }

        // 예약된 알림 발행
        scheduledNotificationPublisher.publish()
    }
}
