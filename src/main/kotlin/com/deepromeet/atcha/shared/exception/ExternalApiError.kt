package com.deepromeet.atcha.shared.exception

import org.springframework.boot.logging.LogLevel

enum class ExternalApiError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    EXTERNAL_API_BAD_REQUEST_ERROR(400, "EXT_002", "외부 API 요청이 잘못되었습니다", LogLevel.WARN),
    EXTERNAL_API_FORBIDDEN_ERROR(403, "EXT_004", "외부 API에 접근할 권한이 없습니다", LogLevel.WARN),
    EXTERNAL_API_NOT_FOUND_ERROR(404, "EXT_001", "외부 API에서 데이터를 찾을 수 없습니다", LogLevel.WARN),
    EXTERNAL_API_INTERNAL_SERVER_ERROR(500, "EXT_003", "외부 API 서버에서 오류가 발생했습니다", LogLevel.ERROR),
    EXTERNAL_API_UNKNOWN_ERROR(500, "EXT_005", "외부 API 호출 중 알 수 없는 오류가 발생했습니다", LogLevel.ERROR),
    EXTERNAL_API_CALL_LIMIT_EXCEEDED(429, "EXT_006", "외부 API 호출 제한을 초과했습니다.", LogLevel.ERROR),
    EXTERNAL_API_CIRCUIT_BREAKER_OPEN(503, "EXT_007", "외부 서비스가 일시적으로 이용 불가능합니다.", LogLevel.WARN)
}

class ExternalApiException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): ExternalApiException {
            return ExternalApiException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): ExternalApiException {
            return ExternalApiException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): ExternalApiException {
            return ExternalApiException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): ExternalApiException {
            return ExternalApiException(errorType, customMessage = message, cause = cause)
        }
    }
}
