package com.deepromeet.seulseul.user.api.controller

import com.deepromeet.seulseul.common.BaseControllerTest
import com.deepromeet.seulseul.common.token.TokenGenerator
import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


class UserControllerTest(
    @Autowired
    private val tokenGenerator: TokenGenerator
) : BaseControllerTest() {
    var accessToken: String = ""

    @BeforeEach
    fun issueToken() {
        val generateToken = tokenGenerator.generateToken(1L)
        accessToken = generateToken.accessToken
        println("accessToken $accessToken")
    }

    @Test
    fun `유저 정보 조회`() {
        RestAssured.given().log().all()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/members/me")
            .then().log().all()
            .statusCode(200)
    }
}
