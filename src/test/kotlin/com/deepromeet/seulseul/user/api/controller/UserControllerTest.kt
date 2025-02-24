package com.deepromeet.seulseul.user.api.controller

import com.deepromeet.seulseul.common.BaseControllerTest
import com.deepromeet.seulseul.common.token.TokenGenerator
import com.deepromeet.seulseul.common.web.ApiResponse
import com.deepromeet.seulseul.user.domain.User
import com.deepromeet.seulseul.user.domain.UserReader
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.restassured.RestAssured
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


class UserControllerTest(
    @Autowired
    private val tokenGenerator: TokenGenerator,
    @Autowired
    private val userReader: UserReader
) : BaseControllerTest() {
    var accessToken: String = ""
    var savedUser: User = User(kakaoId = 1L, nickname = "유저", thumbnailImageUrl = "", profileImageUrl = "")

    @BeforeEach
    fun issueToken() {
        savedUser = userReader.save(savedUser)
        val generateToken = tokenGenerator.generateToken(savedUser.id)
        accessToken = generateToken.accessToken
    }

    @Test
    fun `유저 정보 조회`() {
        // given
        val result = RestAssured.given().log().all()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/members/me")
            .then().log().all()
            .statusCode(200)
            .extract().`as`(ApiResponse::class.java)
            .result
        val objectMapper = jacksonObjectMapper()
        val findUser: User = objectMapper.convertValue(result, User::class.java)

        // when && then
        Assertions.assertThat(findUser).isEqualTo(savedUser)
    }
}
