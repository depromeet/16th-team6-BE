package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.notification.domatin.UserNotificationReader
import com.deepromeet.atcha.notification.domatin.UserNotificationStreamProducer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

@Component
class ScheduledNotificationPublisher(
    private val userNotificationReader: UserNotificationReader,
    private val userNotificationStreamProducer: UserNotificationStreamProducer
) {
    fun publish() {
        val currentMinute = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        val notifications = userNotificationReader.findByTime(currentMinute)

        if (notifications.isNotEmpty()) {
            userNotificationStreamProducer.produceAll(notifications)
            log.debug { "메시지 큐에 ${notifications.size}개의 정시 알림을 발행했습니다." }
        }
    }
}
