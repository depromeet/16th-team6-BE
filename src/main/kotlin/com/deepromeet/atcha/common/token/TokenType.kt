package com.deepromeet.atcha.common.token

enum class TokenType(
    val expirationMills: Long
) {
//    ACCESS(1000L * 60 * 30), // 30분
    ACCESS(1000L * 15), // 15초
    REFRESH(1000L * 60 * 60 * 24 * 60) // 60일
}
