package com.deepromeet.atcha.auth.application

import com.deepromeet.atcha.auth.domain.Provider
import com.deepromeet.atcha.auth.domain.ProviderToken

interface AuthProvider {
    fun getProviderUserId(providerToken: ProviderToken): Provider

    fun logout(providerToken: String)

    fun logout(provider: Provider)
}
