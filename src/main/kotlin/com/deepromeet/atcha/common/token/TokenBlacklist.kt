package com.deepromeet.atcha.common.token

interface TokenBlacklist {
    fun add(token: String)

    fun contains(token: String): Boolean
}
