package com.deepromeet.atcha.auth.application

import com.deepromeet.atcha.auth.domain.ProviderContext
import com.deepromeet.atcha.auth.domain.ProviderToken

interface AuthProvider {
    suspend fun getProviderContext(providerToken: ProviderToken): ProviderContext

    suspend fun logout(providerToken: String)

    suspend fun logout(providerContext: ProviderContext)
}
