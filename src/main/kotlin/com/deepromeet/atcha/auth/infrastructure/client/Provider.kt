package com.deepromeet.atcha.auth.infrastructure.client

import com.deepromeet.atcha.auth.exception.AuthException

enum class Provider(
    val clientBeanName: String
) {
    KAKAO("kakaoClient")
    ;

    companion object {
        fun findByOrdinal(ordinal: Int): Provider {
            return when (ordinal) {
                1 -> KAKAO
                else -> throw AuthException.NoMatchedProvider
            }
        }
    }
}
