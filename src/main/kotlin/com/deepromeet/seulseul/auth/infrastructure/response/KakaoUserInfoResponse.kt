package com.deepromeet.seulseul.auth.infrastructure.response


data class KakaoUserInfoResponse(
    val kakaoId: Long,
    val kakaoAccount: KakaoAccount
) {
    val nickname: String get() = kakaoAccount.profile.nickname
    val thumbnailImageUrl: String get() = kakaoAccount.profile.thumbnailImageUrl
    val profileImageUrl: String get() = kakaoAccount.profile.profileImageUrl
    val profile: Profile get() = kakaoAccount.profile
}

data class KakaoAccount(
    val profile: Profile
)

data class Profile(
    val nickname: String,
    val thumbnailImageUrl: String,
    val profileImageUrl: String,
)
