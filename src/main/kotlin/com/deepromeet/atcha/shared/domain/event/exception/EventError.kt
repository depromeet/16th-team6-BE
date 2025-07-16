package com.deepromeet.atcha.shared.domain.event.exception

import com.deepromeet.atcha.shared.exception.BaseErrorType
import com.deepromeet.atcha.shared.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class EventError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    INVALID_EVENT(500, "EVT_001", "이벤트 형식이 잘못되었습니다.", LogLevel.ERROR)
}

class EventException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): EventException {
            return EventException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): EventException {
            return EventException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): EventException {
            return EventException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): EventException {
            return EventException(errorType, customMessage = message, cause = cause)
        }
    }
}
