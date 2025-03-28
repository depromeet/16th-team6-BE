package com.deepromeet.atcha.auth.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
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

sealed class AuthException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object NoMatchedProvider : AuthException(AuthError.NO_MATCHED_PROVIDER) {
        override fun readResolve(): Any = NoMatchedProvider
    }

    data object AlreadyExistsUser : AuthException(AuthError.ALREADY_EXISTS_USER) {
        override fun readResolve(): Any = AlreadyExistsUser
    }

    data object NoMatchedUserToken : AuthException(AuthError.NO_MATCHED_USER_TOKEN) {
        override fun readResolve(): Any = NoMatchedUserToken
    }
}
