package com.deepromeet.atcha.auth.application

import com.deepromeet.atcha.auth.domain.ProviderContext
import com.deepromeet.atcha.auth.domain.ProviderToken

interface AuthProvider {
    fun getProviderContext(providerToken: ProviderToken): ProviderContext

    fun logout(providerToken: String)

    fun logout(providerContext: ProviderContext)
}
