package com.deepromeet.atcha.app.exception

import com.deepromeet.atcha.shared.exception.BaseErrorType
import com.deepromeet.atcha.shared.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class AppError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    NO_MATCHED_PLATFORM(404, "APP_001", "일치하는 플랫폼이 없습니다.", LogLevel.WARN)
}

class AppException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): AppException {
            return AppException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): AppException {
            return AppException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): AppException {
            return AppException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): AppException {
            return AppException(errorType, customMessage = message, cause = cause)
        }
    }
}
