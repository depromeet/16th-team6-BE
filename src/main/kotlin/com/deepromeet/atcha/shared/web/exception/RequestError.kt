package com.deepromeet.atcha.shared.web.exception

import com.deepromeet.atcha.shared.exception.BaseErrorType
import com.deepromeet.atcha.shared.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class RequestError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    NO_REQUEST_INFO(400, "REQ_001", "요청 정보가 존재하지 않습니다.", LogLevel.WARN),
    NOT_VALID_HEADER(400, "REQ_002", "잘못된 헤더 형식입니다.", LogLevel.WARN),
    NO_MATCHED_METHOD(400, "REQ_003", "잘못된 API 요청입니다.", LogLevel.WARN),
    NO_MATCHED_RESOURCE(400, "REQ_004", "잘못된 API 요청입니다.", LogLevel.WARN)
}

class RequestException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): RequestException {
            return RequestException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): RequestException {
            return RequestException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): RequestException {
            return RequestException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): RequestException {
            return RequestException(errorType, customMessage = message, cause = cause)
        }
    }
}
