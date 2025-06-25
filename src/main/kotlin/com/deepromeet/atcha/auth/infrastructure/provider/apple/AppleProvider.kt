package com.deepromeet.atcha.auth.infrastructure.provider.apple

import com.deepromeet.atcha.auth.domain.AuthProvider
import com.deepromeet.atcha.auth.domain.Provider
import com.deepromeet.atcha.auth.infrastructure.response.ProviderUserInfoResponse
import org.springframework.stereotype.Component

@Component
class AppleProvider(
    private val appleFeignClient: AppleFeignClient,
    private val appleTokenParser: AppleTokenParser
) : AuthProvider {
    override fun getUserInfo(providerToken: String): ProviderUserInfoResponse {
        val header = appleTokenParser.parseHeader(providerToken)
        val publicKeys = appleFeignClient.getPublicKeys()
        val publicKey = PublicKeyGenerator.generate(header, publicKeys)
        val claims = appleTokenParser.extractClaims(providerToken, publicKey)

        return ProviderUserInfoResponse(providerId = claims.subject)
    }

    override fun logout(providerToken: String) {
        TODO("Not yet implemented")
    }

    override fun logout(provider: Provider) {
        TODO("Not yet implemented")
    }
}
