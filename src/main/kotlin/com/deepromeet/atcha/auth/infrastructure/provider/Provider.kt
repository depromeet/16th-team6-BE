package com.deepromeet.atcha.auth.infrastructure.provider

import com.deepromeet.atcha.auth.exception.AuthException

enum class Provider(
    val beanName: String
) {
    KAKAO("kakaoProvider")
    ;

    companion object {
        fun findByOrdinal(ordinal: Int): Provider {
            return when (ordinal) {
                0 -> KAKAO
                else -> throw AuthException.NoMatchedProvider
            }
        }
    }
}
