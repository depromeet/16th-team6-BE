package com.deepromeet.atcha.auth.domain

@JvmInline
value class UserProviderId(val value: Long) {
    init {
        require(value >= 0) { "UserProvider ID must be non-negative" }
    }
}
