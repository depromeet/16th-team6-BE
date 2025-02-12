package com.deepromeet.seulseul.common.exception

open class WebException(errorCode: BaseErrorType) :
        CustomException(errorCode, "웹 계층 예외")
