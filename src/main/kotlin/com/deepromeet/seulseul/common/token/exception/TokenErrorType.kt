package com.deepromeet.seulseul.common.token.exception

import com.deepromeet.seulseul.common.exception.BaseErrorType
import com.deepromeet.seulseul.common.exception.ErrorReason
import org.springframework.boot.logging.LogLevel

enum class TokenErrorType(
    private val status: Int,
    private val errorCode: String,
    private val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    EXPIRED_TOKEN(400, "ET_001", "만료된 토큰입니다.", LogLevel.WARN),
    NOT_VALID_TOKEN(400, "NVT_001", "유효하지 않는 토큰입니다.", LogLevel.WARN),
    ;

    override val errorReason: ErrorReason
        get() = ErrorReason(status, errorCode, message)
}
