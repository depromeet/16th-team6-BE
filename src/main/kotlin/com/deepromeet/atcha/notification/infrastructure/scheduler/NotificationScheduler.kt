package com.deepromeet.atcha.notification.infrastructure.scheduler

import com.deepromeet.atcha.notification.domatin.UserNotificationReader
import com.deepromeet.atcha.notification.domatin.UserNotificationStreamProducer
import com.deepromeet.atcha.transit.domain.RouteDepartureTimeRefresher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger { }

@Component
class NotificationScheduler(
    private val routeDepartureTimeRefresher: RouteDepartureTimeRefresher,
    private val userNotificationReader: UserNotificationReader,
    private val userNotificationStreamProducer: UserNotificationStreamProducer
) {
    @Scheduled(cron = "0 * * * * ?")
    fun checkAndSendNotifications() {
        // 알림 업데이트
        routeDepartureTimeRefresher.refresh()

        // 현재 시간 기준으로 분 단위 알림 확인 -> 전송 -> 삭제
        val now = LocalDateTime.now()
        val currentMinute = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        val notifications = userNotificationReader.findByTime(currentMinute)
        userNotificationStreamProducer.produceAll(notifications)
        log.debug { "메시지 큐에 ${notifications.size}개의 알림을 발행했습니다." }
    }
}
