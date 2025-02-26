package com.deepromeet.atcha.user.api.response

import com.deepromeet.atcha.user.domain.User

data class UserInfoResponse(
    val id: Long,
    val kakaoId: Long,
    val nickname: String,
    val thumbnailImageUrl: String,
    val profileImageUrl: String,
    val alertAgreement: Boolean,
    val trackingAgreement: Boolean
) {
    companion object {
        fun from(domain: User) =
            UserInfoResponse(
                domain.id,
                domain.kakaoId,
                domain.nickname,
                domain.thumbnailImageUrl,
                domain.profileImageUrl,
                domain.alertAgreement,
                domain.trackingAgreement
            )
    }
}
