package com.deepromeet.seulseul.auth.infrastructure.client

import com.deepromeet.seulseul.auth.exception.AuthException

enum class Provider(
    val index: Int
) {
    KAKAO(1)
    ;

    companion object {
        fun findByIndex(index: Int) : Provider {
            return when(index) {
                1 -> KAKAO
                else -> throw AuthException.NoMatchedProvider
            }
        }
    }
}

