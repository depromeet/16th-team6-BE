package com.deepromeet.atcha.user.api.response

import com.deepromeet.atcha.user.domain.User

data class UserInfoResponse(
    val email: String,
    val nickname: String
) {
    companion object {
        fun from(domain: User) = UserInfoResponse(domain.nickname)
    }
}
