package com.deepromeet.seulseul.auth.infrastructure.response

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserInfoResponse(
    @JsonProperty("id") val kakaoId: Long
)
