package com.deepromeet.atcha.user.api.response

import com.deepromeet.atcha.user.domain.User

data class UserInfoResponse(
    val id: Long,
    val providerId: Long,
    val nickname: String,
    val profileImageUrl: String,
    val alertAgreement: Boolean,
    val trackingAgreement: Boolean
) {
    companion object {
        fun from(domain: User) =
            UserInfoResponse(
                domain.id,
                domain.providerId,
                domain.nickname,
                domain.profileImageUrl,
                domain.agreement.alert,
                domain.agreement.tracking
            )
    }
}
