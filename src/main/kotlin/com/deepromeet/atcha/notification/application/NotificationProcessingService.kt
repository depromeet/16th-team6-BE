package com.deepromeet.atcha.notification.application

import com.deepromeet.atcha.notification.domain.Messaging
import com.deepromeet.atcha.notification.domain.NotificationContentCreator
import com.deepromeet.atcha.notification.domain.NotificationData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class NotificationProcessingService(
    private val messagingManager: MessagingManager,
    private val notificationContentCreator: NotificationContentCreator,
    private val duplicateChecker: NotificationDuplicateChecker
) {
    fun process(data: NotificationData): NotificationResult {
        if (!duplicateChecker.isNewNotification(data.idempotencyKey)) {
            return NotificationResult.duplicate(data.idempotencyKey)
        }

        return try {
            val content = notificationContentCreator.createPushContent(data)
            val messaging = Messaging(content, data.token)

            if (messagingManager.send(messaging)) {
                NotificationResult.success(data.idempotencyKey)
            } else {
                duplicateChecker.markAsFailed(data.idempotencyKey)
                NotificationResult.failed(data.idempotencyKey)
            }
        } catch (e: Exception) {
            log.error(e) { "알림 처리 중 오류 발생: ${data.idempotencyKey}" }
            duplicateChecker.markAsFailed(data.idempotencyKey)
            NotificationResult.error(data.idempotencyKey, e)
        }
    }
}

data class NotificationResult(
    val status: Status,
    val idempotencyKey: String,
    val error: Exception? = null
) {
    enum class Status { SUCCESS, FAILED, DUPLICATE, ERROR }

    companion object {
        fun success(key: String) = NotificationResult(Status.SUCCESS, key)

        fun failed(key: String) = NotificationResult(Status.FAILED, key)

        fun duplicate(key: String) = NotificationResult(Status.DUPLICATE, key)

        fun error(
            key: String,
            error: Exception
        ) = NotificationResult(Status.ERROR, key, error)
    }
}
