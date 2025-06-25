package com.deepromeet.atcha.auth.infrastructure.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoUserInfoResponse(
    @JsonProperty("id") val kakaoId: Long
) {
    fun toUserInfoResponse(): ProviderUserInfoResponse = ProviderUserInfoResponse(providerId = kakaoId.toString())
}
