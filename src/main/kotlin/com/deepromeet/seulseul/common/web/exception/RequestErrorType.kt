package com.deepromeet.seulseul.common.web.exception

import com.deepromeet.seulseul.common.exception.BaseErrorType
import com.deepromeet.seulseul.common.exception.ErrorReason
import org.springframework.boot.logging.LogLevel

enum class RequestErrorType(
    private val status: Int,
    private val errorCode: String,
    private val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    NO_REQUEST_INFO(400, "NRI_001", "요청 정보가 존재하지 않습니다.", LogLevel.WARN),
    NOT_VALID_HEADER(400, "NVH_001", "잘못된 헤더 형식입니다.", LogLevel.WARN)
    ;

    override val errorReason: ErrorReason
        get() = ErrorReason(status, errorCode, message)
}
