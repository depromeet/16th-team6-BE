package com.deepromeet.atcha.user.domain

@JvmInline
value class UserId(val value: Long) {
    init {
        require(value >= 0) { "User ID must be non-negative" }
    }
}
