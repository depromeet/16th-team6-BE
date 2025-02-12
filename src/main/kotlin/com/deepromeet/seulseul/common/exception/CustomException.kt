package com.deepromeet.seulseul.common.exception

abstract class CustomException(
    val errorType: BaseErrorType,
    private val sourceLayer: String,
) : RuntimeException() {

    val status: Int
        get() = errorType.errorReason.status

    override val message: String
        get() =
                sourceLayer.let { "$it - ${errorType.errorReason.message}" }
}
