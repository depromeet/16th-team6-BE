package com.deepromeet.atcha.auth.infrastructure.response

import com.deepromeet.atcha.user.domain.User
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoUserInfoResponse(
    @JsonProperty("id") val kakaoId: Long,
    val kakaoAccount: KakaoAccount
) {
    val nickname: String get() = kakaoAccount.profile.nickname
    val profileImageUrl: String get() = kakaoAccount.profile.profileImageUrl
    val profile: Profile get() = kakaoAccount.profile

    fun toUserInfoResponse(): ProviderUserInfoResponse =
        ProviderUserInfoResponse(providerId = kakaoId, nickname, profileImageUrl)
}

data class KakaoAccount(
    val profile: Profile
)

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Profile(
    val nickname: String,
    val profileImageUrl: String
)
