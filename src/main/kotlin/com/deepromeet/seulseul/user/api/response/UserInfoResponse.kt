package com.deepromeet.seulseul.user.api.response

import com.deepromeet.seulseul.user.domain.User

data class UserInfoResponse(
    val email: String,
    val nickname: String,
) {
    companion object {
        fun from(domain: User) = UserInfoResponse(domain.email.value, domain.name)
    }
}
