package com.deepromeet.atcha.transit.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import com.deepromeet.atcha.common.exception.ErrorReason
import org.springframework.boot.logging.LogLevel

enum class TransitErrorType(
    private val status: Int,
    private val errorCode: String,
    private val message: String,
    override val logLevel: LogLevel,
) : BaseErrorType {
    TRANSIT_API_ERROR(
        status = 500,
        errorCode = "TRANSIT_API_ERROR",
        message = "대중교통 API 호출 중 에러가 발생했습니다",
        logLevel = LogLevel.ERROR,
    ),
    ;

    override val errorReason: ErrorReason
        get() = ErrorReason(status, errorCode, message)
}

sealed class TransitException(
    errorCode: BaseErrorType,
) : CustomException(errorCode)
