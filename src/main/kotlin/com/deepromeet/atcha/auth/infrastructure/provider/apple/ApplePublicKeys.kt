package com.deepromeet.atcha.auth.infrastructure.provider.apple

data class ApplePublicKeys(
    val keys: List<ApplePublicKey>
) {
    fun getMatchingKey(
        kid: String,
        alg: String
    ): ApplePublicKey {
        return keys.firstOrNull { it.isSameKid(kid) && it.isSameAlg(alg) }
            ?: throw RuntimeException("일치하는 애플 공개 키를 찾을 수 없습니다. kid: $kid, alg: $alg")
    }
}
