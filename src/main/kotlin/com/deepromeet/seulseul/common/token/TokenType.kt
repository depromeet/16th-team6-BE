package com.deepromeet.seulseul.common.token

enum class TokenType(
    val expirationMills: Long
) {
    ACCESS(1000L * 60 * 30),
    REFRESH(1000L * 60 * 60 * 24 * 30),
    ;
}
