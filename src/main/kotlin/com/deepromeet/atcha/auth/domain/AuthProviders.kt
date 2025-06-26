package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.exception.AuthError
import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType
import org.springframework.stereotype.Component

@Component
class AuthProviders(
    private val providers: Map<String, AuthProvider>
) {
    fun getAuthProvider(providerType: ProviderType): AuthProvider {
        return providers[providerType.beanName]
            ?: throw AuthException.of(AuthError.NO_MATCHED_PROVIDER)
    }
}
