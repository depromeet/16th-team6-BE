package com.deepromeet.seulseul.common.exception

data class ErrorReason(
        val status: Int,
        val errorCode: String,
        val message: String,
) {
}
