package com.deepromeet.atcha.auth.infrastructure.provider.apple

data class ApplePublicKey(
    val kty: String,
    val kid: String,
    val use: String,
    val alg: String,
    val n: String,
    val e: String
) {
    fun isSameKid(kid: String): Boolean {
        return this.kid == kid
    }

    fun isSameAlg(alg: String): Boolean {
        return this.alg == alg
    }
}
