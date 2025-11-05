package com.deepromeet.atcha.auth.infrastructure.provider.apple

import com.deepromeet.atcha.auth.application.AuthProvider
import com.deepromeet.atcha.auth.domain.ProviderContext
import com.deepromeet.atcha.auth.domain.ProviderToken
import org.springframework.stereotype.Component

@Component
class AppleProvider(
    private val appleHttpClient: AppleHttpClient,
    private val appleTokenParser: AppleTokenParser
) : AuthProvider {
    override suspend fun getProviderContext(providerToken: ProviderToken): ProviderContext {
        val header = appleTokenParser.parseHeader(providerToken.token)
        val publicKeys = appleHttpClient.getPublicKeys()
        val publicKey = PublicKeyGenerator.generate(header, publicKeys)
        val claims = appleTokenParser.extractClaims(providerToken.token, publicKey)

        return ProviderContext(
            claims.subject,
            providerToken.providerType,
            providerToken.token
        )
    }

    override suspend fun logout(providerToken: String) {
        TODO("Not yet implemented")
    }

    override suspend fun logout(providerContext: ProviderContext) {
        TODO("Not yet implemented")
    }
}
