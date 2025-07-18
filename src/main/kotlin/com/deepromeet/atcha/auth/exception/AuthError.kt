package com.deepromeet.atcha.auth.exception

import com.deepromeet.atcha.shared.exception.BaseErrorType
import com.deepromeet.atcha.shared.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class AuthError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    NO_MATCHED_PROVIDER(400, "ATH_001", "일치하는 로그인 플랫폼이 없습니다.", LogLevel.WARN),
    ALREADY_EXISTS_USER(400, "ATH_002", "이미 존재하는 유저입니다.", LogLevel.WARN),
    NO_MATCHED_USER_TOKEN(400, "ATH_003", "일치하는 유저 토큰이 없습니다.", LogLevel.WARN)
}

class AuthException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): AuthException {
            return AuthException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): AuthException {
            return AuthException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): AuthException {
            return AuthException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): AuthException {
            return AuthException(errorType, customMessage = message, cause = cause)
        }
    }
}
