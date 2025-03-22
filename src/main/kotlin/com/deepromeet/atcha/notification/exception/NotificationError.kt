package com.deepromeet.atcha.notification.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class NotificationErrorType(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    INVALID_ROUTE_ID(400, "NTF_001", "유효하지 않은 경로 ID 입니다.", LogLevel.ERROR)
}

sealed class NotificationException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object InvalidRouteId : NotificationException(NotificationErrorType.INVALID_ROUTE_ID) {
        override fun readResolve(): Any = InvalidRouteId
    }
}
