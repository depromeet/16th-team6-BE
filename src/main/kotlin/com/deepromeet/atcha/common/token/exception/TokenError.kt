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
    EXPIRED_TOKEN(400, "ET_001", "만료된 토큰입니다.", LogLevel.WARN),
    NOT_VALID_TOKEN(400, "NVT_001", "유효하지 않는 토큰입니다.", LogLevel.WARN)
}

sealed class TokenException(
    errorType: TokenError
) : CustomException(errorType) {
    data object ExpiredToken : TokenException(TokenError.EXPIRED_TOKEN) {
        override fun readResolve(): Any = ExpiredToken
    }

    data object NotValidToken : TokenException(TokenError.NOT_VALID_TOKEN) {
        override fun readResolve(): Any = NotValidToken
    }
}
