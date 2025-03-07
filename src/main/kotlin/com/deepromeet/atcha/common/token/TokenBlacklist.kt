package com.deepromeet.atcha.common.token

interface TokenBlacklist {
    fun add(
        token: String,
        tokenType: TokenType
    )

    fun has(token: String): Boolean
}
