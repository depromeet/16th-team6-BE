package com.deepromeet.atcha.transit.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class TransitErrorType(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {

    ;

}

sealed class TransitException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {

}
