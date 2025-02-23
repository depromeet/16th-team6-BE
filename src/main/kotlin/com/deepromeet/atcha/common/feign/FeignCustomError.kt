package com.deepromeet.atcha.common.feign

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import com.deepromeet.atcha.common.exception.ErrorReason
import org.springframework.boot.logging.LogLevel

enum class FeignErrorType(
    private val status: Int,
    private val errorCode: String,
    private val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {

    EXTERNAL_API_BAD_REQUEST_ERROR(400, "EXT_002", "외부 API 요청이 잘못되었습니다", LogLevel.WARN),
    EXTERNAL_API_NOT_FOUND_ERROR(404, "EXT_001", "외부 API에서 데이터를 찾을 수 없습니다", LogLevel.WARN),
    EXTERNAL_API_INTERNAL_SERVER_ERROR(500, "EXT_003", "외부 API 서버에서 오류가 발생했습니다", LogLevel.ERROR),
    ;

    override val errorReason: ErrorReason
        get() = ErrorReason(status, errorCode, message)
}

sealed class FeignException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {

    data object ExternalApiNotFoundError : FeignException(FeignErrorType.EXTERNAL_API_NOT_FOUND_ERROR) {
        override fun readResolve(): Any = ExternalApiNotFoundError
    }

    data object ExternalApiBadRequestError : FeignException(FeignErrorType.EXTERNAL_API_BAD_REQUEST_ERROR) {
        override fun readResolve(): Any = ExternalApiBadRequestError
    }

    data object ExternalApiInternalServerError : FeignException(FeignErrorType.EXTERNAL_API_INTERNAL_SERVER_ERROR) {
        override fun readResolve(): Any = ExternalApiInternalServerError
    }
}
