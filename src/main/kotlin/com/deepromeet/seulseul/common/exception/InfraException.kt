package com.deepromeet.seulseul.common.exception

open class InfraException(errorCode: BaseErrorType) :
    CustomException(errorCode, "인프라 계층 예외")
