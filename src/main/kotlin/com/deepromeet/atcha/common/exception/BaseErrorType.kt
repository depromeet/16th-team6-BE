package com.deepromeet.atcha.common.exception

import org.springframework.boot.logging.LogLevel

interface BaseErrorType {
    val status: Int
    val errorCode: String
    val message: String
    val logLevel: LogLevel
}
