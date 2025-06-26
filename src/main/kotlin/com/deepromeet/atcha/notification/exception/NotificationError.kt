package com.deepromeet.atcha.notification.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class NotificationError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    INVALID_ROUTE_ID(400, "NTF_001", "유효하지 않은 경로 ID 입니다.", LogLevel.ERROR)
}

class NotificationException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): NotificationException {
            return NotificationException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): NotificationException {
            return NotificationException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): NotificationException {
            return NotificationException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): NotificationException {
            return NotificationException(errorType, customMessage = message, cause = cause)
        }
    }
}
