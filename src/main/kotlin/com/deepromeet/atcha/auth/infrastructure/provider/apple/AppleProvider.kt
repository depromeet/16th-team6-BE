package com.deepromeet.atcha.auth.infrastructure.provider.apple

import com.deepromeet.atcha.auth.domain.AuthProvider
import com.deepromeet.atcha.auth.domain.Provider
import com.deepromeet.atcha.auth.domain.ProviderToken
import org.springframework.stereotype.Component

@Component
class AppleProvider(
    private val appleFeignClient: AppleFeignClient,
    private val appleTokenParser: AppleTokenParser
) : AuthProvider {
    override fun getProviderUserId(providerToken: ProviderToken): Provider {
        val header = appleTokenParser.parseHeader(providerToken.token)
        val publicKeys = appleFeignClient.getPublicKeys()
        val publicKey = PublicKeyGenerator.generate(header, publicKeys)
        val claims = appleTokenParser.extractClaims(providerToken.token, publicKey)

        return Provider(
            claims.subject,
            providerToken.providerType,
            providerToken.token
        )
    }

    override fun logout(providerToken: String) {
        TODO("Not yet implemented")
    }

    override fun logout(provider: Provider) {
        TODO("Not yet implemented")
    }
}
