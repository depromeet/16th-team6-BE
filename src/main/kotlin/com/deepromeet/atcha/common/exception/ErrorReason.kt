package com.deepromeet.atcha.common.exception

data class ErrorReason(
    val status: Int,
    val errorCode: String,
    val message: String,
)
