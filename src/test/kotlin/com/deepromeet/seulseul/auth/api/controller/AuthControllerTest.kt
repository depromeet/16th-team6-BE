package com.deepromeet.seulseul.auth.api.controller

import com.deepromeet.seulseul.auth.api.request.SignUpRequest
import com.deepromeet.seulseul.auth.api.request.Terms
import com.deepromeet.seulseul.auth.infrastructure.response.KakaoAccount
import com.deepromeet.seulseul.auth.infrastructure.response.KakaoUserInfoResponse
import com.deepromeet.seulseul.auth.infrastructure.response.Profile
import com.deepromeet.seulseul.common.BaseControllerTest
import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`

class AuthControllerTest : BaseControllerTest() {
    val accessToken: String = "thisisfortestfJmGasdwdWIDEbraTFAAAAAQoqJREAAAGVMPfFQEA9X5YOsAdz"
    val kakaoId = 12345L
    val profile = Profile("test", "test@test.com", "testUrl")
    val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))

    // todo beforeEach로 저장된 유저 정보 지우기
    @BeforeEach
    fun setMockKakaoApiClient() {
        `when`(kakaoApiClient.getUserInfo(anyString())).thenReturn(kakaoUserInfo)
    }

    @Test
    fun 회원가입() {
        val signUpRequest = SignUpRequest(
            "경기도 화성시 동탄순환대로26길 21",
            "37.207581",
            "127.113558",
            Terms(true)
        )

        RestAssured.given().log().all()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .body(signUpRequest)
            .`when`().post("/api/auth/sign-up")
            .then().log().all()
            .statusCode(201)
    }

    @Test
    fun `존재하는 유저`() {
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/auth/check")
            .then().log().all()
            .statusCode(200)
    }

    @Test
    fun `로그인 요청`() {
        // given : 회원가입
        val signUpRequest = SignUpRequest(
            "경기도 화성시 동탄순환대로26길 21",
            "37.207581",
            "127.113558",
            Terms(true)
        )
        RestAssured.given().log().all()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .body(signUpRequest)
            .`when`().post("/api/auth/sign-up")
            .then().log().all()

        // when & then
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/auth/login")
            .then().log().all()
            .statusCode(200)
    }
}
