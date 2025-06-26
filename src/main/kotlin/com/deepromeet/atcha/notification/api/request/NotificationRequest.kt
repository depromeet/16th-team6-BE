package com.deepromeet.atcha.notification.api.request

import com.deepromeet.atcha.notification.exception.NotificationError
import com.deepromeet.atcha.notification.exception.NotificationException

data class NotificationRequest(
    val lastRouteId: String
) {
    init {
        require(lastRouteId.isNotBlank()) {
            throw NotificationException.of(
                NotificationError.INVALID_ROUTE_ID,
                "경로 ID는 비어있을 수 없습니다. 입력된 값: '$lastRouteId'"
            )
        }
    }
}
