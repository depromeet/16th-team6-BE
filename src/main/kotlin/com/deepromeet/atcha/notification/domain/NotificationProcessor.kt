package com.deepromeet.atcha.notification.domain

import com.deepromeet.atcha.userroute.domain.UserRoute
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class NotificationProcessor(
    private val messagingManager: MessagingManager,
    private val notificationContentManager: NotificationContentManager,
    private val duplicateChecker: NotificationDuplicateChecker
) {
    fun process(userRoute: UserRoute): NotificationResult {
        val idempotencyKey = createIdempotencyKey(userRoute)

        if (!duplicateChecker.isNewNotification(idempotencyKey)) {
            return NotificationResult.duplicate(idempotencyKey)
        }

        return try {
            val content =
                notificationContentManager.createPushNotification(
                    userRoute,
                    NotificationType.REFRESH
                )
            val messaging = Messaging(content, userRoute.token)

            if (messagingManager.send(messaging)) {
                log.info { "푸시 전송 성공" }
                NotificationResult.success(idempotencyKey)
            } else {
                log.warn { "알림 전송 실패: $idempotencyKey" }
                duplicateChecker.markAsFailed(idempotencyKey)
                NotificationResult.failed(idempotencyKey)
            }
        } catch (e: Exception) {
            log.error(e) { "알림 처리 중 오류 발생: $idempotencyKey" }
            duplicateChecker.markAsFailed(idempotencyKey)
            NotificationResult.error(idempotencyKey, e)
        }
    }

    private fun createIdempotencyKey(userRoute: UserRoute): String {
        return "notification:processed:${userRoute.userId}:${userRoute.lastRouteId}:${userRoute.updatedAt}"
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
