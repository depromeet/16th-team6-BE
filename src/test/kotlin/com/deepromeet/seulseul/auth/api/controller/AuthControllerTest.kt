package com.deepromeet.seulseul.auth.api.controller

import io.restassured.RestAssured
import org.junit.jupiter.api.Test

class AuthControllerTest {
    val accessToken: String = "W4y70kleS8upJkWyWJZHAGhdUIKnMRwoAAAAAQorDNIAAAGVIvlfTEA9X5YOsAdz"

    @Test
    fun 토큰_정보_요청() {
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/auth/login")
            .then().log().all()
            .statusCode(200)
    }

    @Test
    fun 존재하는_유저() {
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/auth/check")
            .then().log().all()
            .statusCode(200)
    }


    @Test
    fun 로그인_요청() {
        RestAssured.given().log().all()
            .param("provider", "1")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/auth/login")
            .then().log().all()
            .statusCode(200)
    }
}
