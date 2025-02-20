package com.deepromeet.atcha.common.exception

abstract class CustomException(
    val errorType: BaseErrorType
) : RuntimeException() {

    abstract fun readResolve(): Any

    val status: Int
        get() = errorType.errorReason.status

    override val message: String
        get() = errorType.errorReason.message
}
