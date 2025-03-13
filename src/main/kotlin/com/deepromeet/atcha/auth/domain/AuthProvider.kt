package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.response.ProviderUserInfoResponse

interface AuthProvider {
    fun getUserInfo(providerToken: String): ProviderUserInfoResponse

    fun logout(providerToken: String)

    fun logout(provider: Provider)
}
