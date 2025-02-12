package com.deepromeet.seulseul.common.exception

open class DomainException(errorCode: BaseErrorType) : CustomException(errorCode, "도메인 계층 예외") {

}
