package com.deepromeet.seulseul.auth.api.controller

import com.deepromeet.seulseul.auth.api.request.SignUpRequest
import com.deepromeet.seulseul.auth.api.request.Terms
import io.restassured.RestAssured
import org.junit.jupiter.api.Test

class AuthControllerTest {
    val accessToken: String = "LX75YMPA-uoQkDCOIB_gORrzzozDYHALAAAAAQo9c00AAAGVJymDtEA9X5YOsAdz"

    @Test
    fun 토큰_정보_요청() {
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/auth/login")
            .then().log().all()
            .statusCode(200)
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
    fun 존재하는_유저() {
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/auth/check")
            .then().log().all()
            .statusCode(200)
    }

    @Test
    fun 로그인_요청() {
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/auth/login")
            .then().log().all()
            .statusCode(200)
    }
}
