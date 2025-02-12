package com.deepromeet.seulseul.common.exception

import org.springframework.boot.logging.LogLevel

interface BaseErrorType {
    val errorReason: ErrorReason
    val logLevel: LogLevel
}
