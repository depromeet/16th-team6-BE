package com.deepromeet.atcha.common.web.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class RequestError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    NO_REQUEST_INFO(500, "REQ_001", "요청 정보가 존재하지 않습니다.", LogLevel.ERROR),
    NOT_VALID_HEADER(400, "REQ_002", "잘못된 헤더 형식입니다.", LogLevel.WARN),
    NO_MATCHED_METHOD(400, "REQ_003", "잘못된 API 요청입니다.", LogLevel.WARN),
    NO_MATCHED_RESOURCE(400, "REQ_004", "잘못된 API 요청입니다.", LogLevel.WARN)
}

sealed class RequestException(
    errorType: BaseErrorType
) : CustomException(errorType) {
    data object NoRequestInfo : RequestException(RequestError.NO_REQUEST_INFO) {
        override fun readResolve(): Any = NoRequestInfo
    }

    data object NotValidHeader : RequestException(RequestError.NOT_VALID_HEADER) {
        override fun readResolve(): Any = NotValidHeader
    }

    data object NoMatchedMethod : RequestException(RequestError.NO_MATCHED_METHOD) {
        override fun readResolve(): Any = NoMatchedMethod
    }

    data object NoMatchedResource : RequestException(RequestError.NO_MATCHED_RESOURCE) {
        override fun readResolve(): Any = NoMatchedResource
    }
}
