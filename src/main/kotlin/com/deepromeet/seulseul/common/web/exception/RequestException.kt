package com.deepromeet.seulseul.common.web.exception

import com.deepromeet.seulseul.common.exception.BaseErrorType
import com.deepromeet.seulseul.common.exception.WebException

sealed class RequestException(
    errorType: BaseErrorType
) : WebException(errorType) {

    data object NoRequestInfo : RequestException(RequestErrorType.NO_REQUEST_INFO) {
        private fun readResolve(): Any = NoRequestInfo
    }

    data object NotValidHeader : RequestException(RequestErrorType.NOT_VALID_HEADER) {
        private fun readResolve(): Any = NotValidHeader
    }
}
