package com.deepromeet.atcha.user.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class UserError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    NOTIFICATION_NOT_FOUND(404, "USR_001", "해당 ID의 알림이 없습니다.", LogLevel.WARN),
    USER_NOT_FOUND(404, "USR_002", "해당 유저 정보가 존재하지 않습니다.", LogLevel.WARN)
}

class UserException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): UserException {
            return UserException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): UserException {
            return UserException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): UserException {
            return UserException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): UserException {
            return UserException(errorType, customMessage = message, cause = cause)
        }
    }
}
