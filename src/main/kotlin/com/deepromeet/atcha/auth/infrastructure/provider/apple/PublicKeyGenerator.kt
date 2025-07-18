package com.deepromeet.atcha.auth.infrastructure.provider.apple

import java.math.BigInteger
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.RSAPublicKeySpec
import java.util.Base64

object PublicKeyGenerator {
    const val SIGN_ALGORITHM_HEADER = "alg"
    const val KEY_ID_HEADER = "kid"
    const val POSITIVE_SIGN_NUMBER = 1

    fun generate(
        headers: Map<String, String>,
        publicKeys: ApplePublicKeys
    ): PublicKey {
        val alg =
            headers[SIGN_ALGORITHM_HEADER]
                ?: throw IllegalArgumentException("Header '$SIGN_ALGORITHM_HEADER' is required")
        val kid = (
            headers[KEY_ID_HEADER]
                ?: throw IllegalArgumentException("Header '$KEY_ID_HEADER' is required")
        )

        val applePublicKey = publicKeys.getMatchingKey(kid, alg)

        return generatePublicKey(applePublicKey)
    }

    fun generatePublicKey(applePublicKey: ApplePublicKey): PublicKey {
        val nBytes: ByteArray? = Base64.getUrlDecoder().decode(applePublicKey.n)
        val eBytes: ByteArray? = Base64.getUrlDecoder().decode(applePublicKey.e)

        val n = BigInteger(POSITIVE_SIGN_NUMBER, nBytes)
        val e = BigInteger(POSITIVE_SIGN_NUMBER, eBytes)
        val rsaPublicKeySpec = RSAPublicKeySpec(n, e)

        try {
            val keyFactory: KeyFactory = KeyFactory.getInstance(applePublicKey.kty)
            return keyFactory.generatePublic(rsaPublicKeySpec)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("애플 키가 잘못되었습니다.")
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException("애플 키가 잘못되었습니다.")
        }
    }
}
