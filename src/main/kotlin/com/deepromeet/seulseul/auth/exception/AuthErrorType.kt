package com.deepromeet.seulseul.auth.exception

import com.deepromeet.seulseul.common.exception.BaseErrorType
import com.deepromeet.seulseul.common.exception.ErrorReason
import org.springframework.boot.logging.LogLevel

enum class AuthErrorType(
    private val status: Int,
    private val errorCode: String,
    private val message: String,
    override  val logLevel: LogLevel
) : BaseErrorType {
    NO_MATCHED_PROVIDER(400, "NMP_001", "일치하는 로그인 플랫폼이 없습니다.", LogLevel.WARN)
    ;

    override val errorReason: ErrorReason
        get() = ErrorReason(status, errorCode, message)
}
