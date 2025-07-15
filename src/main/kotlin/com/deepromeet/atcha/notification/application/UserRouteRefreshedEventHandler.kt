package com.deepromeet.atcha.notification.application

import com.deepromeet.atcha.notification.domain.RouteRefreshNotificationData
import com.deepromeet.atcha.shared.domain.event.domain.EventHandler
import com.deepromeet.atcha.userroute.domain.UserRoute
import com.deepromeet.atcha.userroute.domain.event.UserRouteRefreshedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class UserRouteRefreshedEventHandler(
    private val notificationProcessingService: NotificationProcessingService,
    private val objectMapper: ObjectMapper
) : EventHandler {
    override fun supports(eventType: String): Boolean {
        return eventType == "USER_ROUTE_REFRESHED"
    }

    override fun handle(eventPayload: String) {
        val event = objectMapper.readValue(eventPayload, UserRouteRefreshedEvent::class.java)

        val notificationData =
            RouteRefreshNotificationData(
                userId = event.userRoute.userId.toString(),
                token = event.userRoute.token,
                idempotencyKey = createIdempotencyKey(event.userRoute),
                departureTime = event.userRoute.departureTime,
                updatedAt = event.userRoute.updatedAt
            )

        val result = notificationProcessingService.process(notificationData)

        when (result.status) {
            NotificationResult.Status.SUCCESS -> {
                log.info { "UserRoute 갱신 알림 처리 성공: ${result.idempotencyKey}" }
            }
            NotificationResult.Status.DUPLICATE -> {
                log.debug { "중복 처리된 알림: ${result.idempotencyKey}" }
            }
            else -> {
                log.warn { "UserRoute 갱신 알림 처리 실패: ${result.idempotencyKey}" }
            }
        }
    }

    private fun createIdempotencyKey(userRoute: UserRoute): String {
        return "notification:processed:${userRoute.userId}:${userRoute.lastRouteId}:${userRoute.updatedAt}"
    }
}
