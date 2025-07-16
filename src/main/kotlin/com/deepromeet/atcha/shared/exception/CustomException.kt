package com.deepromeet.atcha.shared.exception

abstract class CustomException(
    val errorType: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : RuntimeException(customMessage ?: errorType.message, cause) {
    abstract fun readResolve(): Any

    val status: Int
        get() = errorType.status
}
