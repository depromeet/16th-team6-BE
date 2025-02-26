package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.response.ClientUserInfoResponse

interface AuthClient {

    fun getUserInfo(providerToken: String): ClientUserInfoResponse

    fun logout(providerToken: String)
}
