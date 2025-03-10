package com.deepromeet.atcha.common.token

enum class TokenType(
    val expirationMills: Long
) {
//    ACCESS(1000L * 60 * 30), // 30분
    ACCESS(1000L * 60 * 60 * 24 * 30 * 6), // TODO 추후 삭제
    REFRESH(1000L * 60 * 60 * 24 * 30 * 6) // 60일
}
