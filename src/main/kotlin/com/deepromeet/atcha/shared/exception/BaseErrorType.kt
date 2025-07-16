package com.deepromeet.atcha.shared.exception

import org.springframework.boot.logging.LogLevel

interface BaseErrorType {
    val status: Int
    val errorCode: String
    val message: String
    val logLevel: LogLevel
}
