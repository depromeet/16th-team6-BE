package com.deepromeet.atcha.auth.infrastructure.response

import com.deepromeet.atcha.user.domain.User

data class ClientUserInfoResponse(
    val clientId: Long,
    val nickname: String,
    val profileImageUrl: String,
) {
    fun toDomain(): User = User(
        providerId = clientId,
        nickname =  nickname,
        profileImageUrl = profileImageUrl
    )
}
