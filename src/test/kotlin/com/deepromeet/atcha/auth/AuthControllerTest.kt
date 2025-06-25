package com.deepromeet.atcha.auth

import com.deepromeet.atcha.auth.api.response.LoginResponse
import com.deepromeet.atcha.auth.api.response.SignUpResponse
import com.deepromeet.atcha.auth.infrastructure.response.KakaoUserInfoResponse
import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.support.BaseControllerTest
import com.deepromeet.atcha.support.fixture.UserFixture
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.http.HttpHeaders

class AuthControllerTest : BaseControllerTest() {
    private val providerAccessToken: String = "thisisfortestfJmGasdwdWIDEbraTFAAAAAQoqJREAAAGVMPfFQEA9X5YOsAdz"
    private val providerId = 12345L
    private val kakaoUserInfo = KakaoUserInfoResponse(providerId)

    @BeforeEach
    fun setMockKakaoApiClient() {
        `when`(kakaoFeignClient.getUserInfo(anyString())).thenReturn(kakaoUserInfo)
    }

    @Test
    fun `회원가입`() {
        // given
        val user = UserFixture.create()
        val provider = 0
        val signUpRequest = UserFixture.userToSignUpRequest(user, provider)

        // when & then
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
            .header("Authorization", "Bearer $providerAccessToken")
            .param("provider", 0)
            .`when`().get("/api/auth/check")
            .then().log().all()
            .statusCode(200)
    }

    @Test
    fun `로그인 요청`() {
        // given : 회원가입
        signUpUser()

        // when & then
        RestAssured.given().log().all()
            .param("provider", 0)
            .header("Authorization", "Bearer $providerAccessToken")
            .param("fcmToken", "TEST_FCM_TOKEN")
            .`when`().get("/api/auth/login")
            .then().log().all()
            .statusCode(200)
    }

    @Test
    fun `로그아웃`() {
        // given 회원가입 + 로그인
        val signUpResponse = signUpUser()

        // when & then
        RestAssured.given().log().all()
            .header("Authorization", "Bearer ${signUpResponse.refreshToken}")
            .param("fcmToken", "TEST_FCM_TOKEN")
            .`when`().post("/api/auth/logout")
            .then().log().all()
            .statusCode(204)
    }

    @Test
    fun `회원 탈퇴한 유저는 더이상 존재하지 않는다`() {
        val signUpResponse = signUpUser()

        // when
        RestAssured.given().log().all()
            .header("Authorization", "Bearer ${signUpResponse.refreshToken}")
            .param("fcmToken", "TEST_FCM_TOKEN")
            .`when`().post("/api/auth/logout")
            .then().log().all()
            .statusCode(204)

        // then
        RestAssured.given().log().all()
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${signUpResponse.accessToken}")
            .`when`().delete("/api/members/me")
            .then().log().all()
            .statusCode(400)
    }

    @Test
    fun `토큰 재발급`() {
        // given : 회원가입
        val signUpResponse = signUpUser()

        // when & then
        RestAssured.given().log().all()
            .header("Authorization", "Bearer ${signUpResponse.refreshToken}")
            .`when`().get("/api/auth/reissue")
            .then().log().all()
            .statusCode(200)
    }

    @Test
    fun `로그아웃 한 유저는 토큰을 사용할 수 없다`() {
        // given
        val signUpResponse = signUpUser()

        // when
        // 로그인
        RestAssured.given().log().all()
            .param("provider", 0)
            .param("fcmToken", "TEST_FCM_TOKEN")
            .header("Authorization", "Bearer $providerAccessToken")
            .`when`().get("/api/auth/login")
            .then().log().all()
            .statusCode(200)
        // 로그아웃
        RestAssured.given().log().all()
            .header("Authorization", "Bearer ${signUpResponse.refreshToken}")
            .param("fcmToken", "TEST_FCM_TOKEN")
            .`when`().post("/api/auth/logout")
            .then().log().all()
            .statusCode(204)

        // then
        RestAssured.given().log().all()
            .header("Authorization", "Bearer ${signUpResponse.refreshToken}")
            .`when`().get("/api/auth/reissue")
            .then().log().all()
            .statusCode(400)
    }

    @Test
    fun `로그아웃 한 유저가 로그인 후 다시 로그아웃한다`() {
        val signUpResponse = signUpUser()

        // 로그인
        RestAssured.given().log().all()
            .param("provider", 0)
            .param("fcmToken", "TEST_FCM_TOKEN")
            .header("Authorization", "Bearer $providerAccessToken")
            .`when`().get("/api/auth/login")
            .then().log().all()
            .statusCode(200)

        // 로그아웃
        RestAssured.given().log().all()
            .header("Authorization", "Bearer ${signUpResponse.refreshToken}")
            .param("fcmToken", "TEST_FCM_TOKEN")
            .`when`().post("/api/auth/logout")
            .then().log().all()
            .statusCode(204)

        // 재로그인
        val result =
            RestAssured.given().log().all()
                .param("provider", 0)
                .param("fcmToken", "TEST_FCM_TOKEN")
                .header("Authorization", "Bearer $providerAccessToken")
                .`when`().get("/api/auth/login")
                .then().log().all()
                .extract().`as`(ApiResponse::class.java)
                .result
        val objectMapper = jacksonObjectMapper()
        val reLoginResponse: LoginResponse = objectMapper.convertValue(result, LoginResponse::class.java)

        // 재로그아웃
        RestAssured.given().log().all()
            .header("Authorization", "Bearer ${reLoginResponse.refreshToken}")
            .param("fcmToken", "TEST_FCM_TOKEN")
            .`when`().post("/api/auth/logout")
            .then().log().all()
            .statusCode(204)
    }

    private fun signUpUser(): SignUpResponse {
        val user = UserFixture.create()
        val provider = 0
        val signUpRequest = UserFixture.userToSignUpRequest(user, provider)

        // when & then
        val result =
            RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $providerAccessToken")
                .body(signUpRequest)
                .`when`().post("/api/auth/sign-up")
                .then().log().all()
                .extract().`as`(ApiResponse::class.java)
                .result
        val objectMapper = jacksonObjectMapper()
        return objectMapper.convertValue(result, SignUpResponse::class.java)
    }
}
