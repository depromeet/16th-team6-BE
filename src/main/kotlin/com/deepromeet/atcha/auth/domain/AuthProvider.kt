package com.deepromeet.atcha.auth.domain

interface AuthProvider {
    fun getProviderUserId(providerToken: ProviderToken): Provider

    fun logout(providerToken: String)

    fun logout(provider: Provider)
}
