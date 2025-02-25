package com.deepromeet.atcha.auth.infrastructure.client

import com.deepromeet.atcha.auth.exception.AuthException

enum class Provider {
    KAKAO
    ;

    companion object {
        fun findByOrdinal(ordinal: Int) : Provider {
            return when(ordinal) {
                1 -> KAKAO
                else -> throw AuthException.NoMatchedProvider
            }
        }
    }
}

