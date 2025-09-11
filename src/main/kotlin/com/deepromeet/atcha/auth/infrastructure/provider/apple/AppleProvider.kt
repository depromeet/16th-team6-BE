package com.deepromeet.atcha.auth.infrastructure.provider.apple

import com.deepromeet.atcha.auth.application.AuthProvider
import com.deepromeet.atcha.auth.domain.ProviderContext
import com.deepromeet.atcha.auth.domain.ProviderToken
import org.springframework.stereotype.Component

@Component
class AppleProvider(
    private val appleFeignClient: AppleFeignClient,
    private val appleTokenParser: AppleTokenParser
) : AuthProvider {
    override fun getProviderContext(providerToken: ProviderToken): ProviderContext {
        val header = appleTokenParser.parseHeader(providerToken.token)
        val publicKeys = appleFeignClient.getPublicKeys()
        val publicKey = PublicKeyGenerator.generate(header, publicKeys)
        val claims = appleTokenParser.extractClaims(providerToken.token, publicKey)

        return ProviderContext(
            claims.subject,
            providerToken.providerType,
            providerToken.token
        )
    }

    override fun logout(providerToken: String) {
        TODO("Not yet implemented")
    }

    override fun logout(providerContext: ProviderContext) {
        TODO("Not yet implemented")
    }
}
