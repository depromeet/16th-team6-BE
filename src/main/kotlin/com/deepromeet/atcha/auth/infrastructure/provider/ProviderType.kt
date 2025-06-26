package com.deepromeet.atcha.auth.infrastructure.provider

import com.deepromeet.atcha.auth.exception.AuthError
import com.deepromeet.atcha.auth.exception.AuthException

enum class ProviderType(
    val beanName: String
) {
    KAKAO("kakaoProvider"),
    APPLE("appleProvider")
    ;

    companion object {
        fun findByOrdinal(ordinal: Int): ProviderType {
            return when (ordinal) {
                0 -> KAKAO
                1 -> APPLE
                else -> throw AuthException.of(
                    AuthError.NO_MATCHED_PROVIDER,
                    "일치하는 플랫폼 ordinal이 없습니다. (ordinal: $ordinal)"
                )
            }
        }
    }
}
