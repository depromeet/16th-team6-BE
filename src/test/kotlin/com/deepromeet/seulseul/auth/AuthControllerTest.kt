package com.deepromeet.seulseul.auth

import com.deepromeet.seulseul.auth.api.request.SignUpRequest
import com.deepromeet.seulseul.auth.api.request.Terms
import com.deepromeet.seulseul.auth.domain.response.SignUpResponse
import com.deepromeet.seulseul.auth.infrastructure.response.KakaoAccount
import com.deepromeet.seulseul.auth.infrastructure.response.KakaoUserInfoResponse
import com.deepromeet.seulseul.auth.infrastructure.response.Profile
import com.deepromeet.seulseul.support.BaseControllerTest
import com.deepromeet.seulseul.common.web.ApiResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`

class AuthControllerTest : BaseControllerTest() {
    private val providerAccessToken: String = "thisisfortestfJmGasdwdWIDEbraTFAAAAAQoqJREAAAGVMPfFQEA9X5YOsAdz"
    private val kakaoId = 12345L
    private val profile = Profile("test", "test@test.com", "testUrl")
    private val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))

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
            Terms(true, true)
        )

        RestAssured.given().log().all()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $providerAccessToken")
            .body(signUpRequest)
            .`when`().post("/api/auth/sign-up")
            .then().log().all()
            .statusCode(201)
    }

    @Test
    fun `존재하는 유저`() {
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $providerAccessToken")
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
            Terms(true, true)
        )
        RestAssured.given().log().all()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $providerAccessToken")
            .body(signUpRequest)
            .`when`().post("/api/auth/sign-up")
            .then().log().all()

        // when & then
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $providerAccessToken")
            .`when`().get("/api/auth/login")
            .then().log().all()
            .statusCode(200)
    }

    @Test
    fun `토큰 재발급`() {
        // given : 회원가입
        val signUpRequest = SignUpRequest(
            "경기도 화성시 동탄순환대로26길 21",
            "37.207581",
            "127.113558",
            Terms(true, true)
        )
        val result = RestAssured.given().log().all()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $providerAccessToken")
            .body(signUpRequest)
            .`when`().post("/api/auth/sign-up")
            .then().log().all()
            .extract().`as`(ApiResponse::class.java)
            .result

        val objectMapper = jacksonObjectMapper()
        val signUpResponse = objectMapper.convertValue(result, SignUpResponse::class.java)

        // when & then
        RestAssured.given().log().all()
            .header("Authorization", "Bearer ${signUpResponse.refreshToken}")
            .`when`().get("/api/auth/reissue")
            .then().log().all()
            .statusCode(200)
    }
}
