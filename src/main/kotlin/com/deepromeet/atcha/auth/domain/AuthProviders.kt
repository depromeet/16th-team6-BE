package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.provider.Provider
import org.springframework.stereotype.Component

@Component
class AuthProviders(
    private val providers: Map<String, AuthProvider>
) {
    fun getAuthProvider(providerOrdinal: Int) : AuthProvider {
        val provider = Provider.findByOrdinal(providerOrdinal)
        return providers[provider.beanName]
            ?: throw AuthException.NoMatchedProvider
    }

    fun getAuthProvider(provider: Provider) : AuthProvider {
        return providers[provider.beanName]
            ?: throw AuthException.NoMatchedProvider
    }
}
