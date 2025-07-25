package com.deepromeet.atcha.user.domain

/**
 * 사용자 식별자 값 객체
 * 도메인에서 Long 타입 대신 명시적인 타입 사용
 */
@JvmInline
value class UserId(val value: Long) {
    init {
        require(value >= 0) { "User ID must be non-negative" }
    }
}
