package com.deepromeet.seulseul.auth.infrastructure.response

import com.deepromeet.seulseul.user.domain.User
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoUserInfoResponse(
    @JsonProperty("id") val kakaoId: Long,
    val kakaoAccount: KakaoAccount
) {
    val nickname: String get() = kakaoAccount.profile.nickname
    val thumbnailImageUrl: String get() = kakaoAccount.profile.thumbnailImageUrl
    val profileImageUrl: String get() = kakaoAccount.profile.profileImageUrl
    val profile: Profile get() = kakaoAccount.profile

    fun toDomain() : User = User(
            kakaoId = kakaoId,
            nickname = nickname,
            thumbnailImageUrl = thumbnailImageUrl,
            profileImageUrl = profileImageUrl
        )
}

data class KakaoAccount(
    val profile: Profile
)

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Profile(
    val nickname: String,
    val thumbnailImageUrl: String,
    val profileImageUrl: String,
)
