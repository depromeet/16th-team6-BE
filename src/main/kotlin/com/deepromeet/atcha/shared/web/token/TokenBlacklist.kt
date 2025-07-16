package com.deepromeet.atcha.shared.web.token

interface TokenBlacklist {
    fun add(token: String)

    fun contains(token: String): Boolean
}
