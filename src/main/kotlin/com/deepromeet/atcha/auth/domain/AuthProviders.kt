package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType
import org.springframework.stereotype.Component

@Component
class AuthProviders(
    private val providers: Map<String, AuthProvider>
) {
    fun getAuthProvider(providerOrdinal: Int) : AuthProvider {
        val providerType = ProviderType.findByOrdinal(providerOrdinal)
        return providers[providerType.beanName]
            ?: throw AuthException.NoMatchedProvider
    }

    fun getAuthProvider(providerType: ProviderType) : AuthProvider {
        return providers[providerType.beanName]
            ?: throw AuthException.NoMatchedProvider
    }

    fun getAuthProvider(provider: Provider) : AuthProvider {
        return providers[provider.providerType.beanName]
            ?: throw AuthException.NoMatchedProvider
    }
}
