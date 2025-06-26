package com.deepromeet.atcha.common.token.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class TokenError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    EXPIRED_TOKEN(400, "TOK_001", "만료된 토큰입니다.", LogLevel.WARN),
    NOT_VALID_TOKEN(400, "TOK_002", "유효하지 않는 토큰입니다.", LogLevel.WARN)
}

class TokenException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): TokenException {
            return TokenException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): TokenException {
            return TokenException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): TokenException {
            return TokenException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): TokenException {
            return TokenException(errorType, customMessage = message, cause = cause)
        }
    }
}
