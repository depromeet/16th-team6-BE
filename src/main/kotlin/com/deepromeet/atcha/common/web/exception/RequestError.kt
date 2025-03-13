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
    NO_REQUEST_INFO(400, "NRI_001", "요청 정보가 존재하지 않습니다.", LogLevel.WARN),
    NOT_VALID_HEADER(400, "NVH_001", "잘못된 헤더 형식입니다.", LogLevel.WARN)
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
}
