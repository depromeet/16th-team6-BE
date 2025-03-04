package com.deepromeet.atcha.user

import com.deepromeet.atcha.common.token.TokenGenerator
import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.support.BaseControllerTest
import com.deepromeet.atcha.user.api.request.UserInfoUpdateRequest
import com.deepromeet.atcha.user.api.response.UserInfoResponse
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserAppender
import com.deepromeet.atcha.user.domain.UserReader
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders

class UserControllerTest(
    @Autowired
    private val tokenGenerator: TokenGenerator,
    @Autowired
    private val userReader: UserReader,
    @Autowired
    private val userAppender: UserAppender
) : BaseControllerTest() {
    var accessToken: String = ""
    var savedUser: User = User(providerId = 1L, nickname = "유저", profileImageUrl = "")

    @BeforeEach
    fun issueToken() {
        savedUser = userAppender.save(savedUser)
        val generateToken = tokenGenerator.generateTokens(savedUser.id)
        accessToken = generateToken.accessToken
    }

    @Test
    fun `회원 정보 조회`() {
        val result =
            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .`when`().get("/api/members/me")
                .then().log().all()
                .statusCode(200)
                .extract().`as`(ApiResponse::class.java)
                .result
        val objectMapper = jacksonObjectMapper()
        val findUser: UserInfoResponse = objectMapper.convertValue(result, UserInfoResponse::class.java)
        assertThat(findUser.id).isEqualTo(savedUser.id)
    }

    @Test
    fun `회원 정보 수정`() {
        // given
        val userInfoUpdateRequest =
            UserInfoUpdateRequest(
                "새로운 닉네임",
                null,
                null,
                null,
                null,
                null,
                null
            )

        // when
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .body(userInfoUpdateRequest)
            .`when`().put("/api/members/me")
            .then().log().all()
            .statusCode(200)

        val findUser = userReader.read(savedUser.id)

        // then
        assertThat(findUser.nickname).isEqualTo(userInfoUpdateRequest.nickname)
    }

    @Test
    fun `회원 삭제`() {
        // given && when
        RestAssured.given().log().all()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .`when`().delete("/api/members/me")
            .then().log().all()
            .statusCode(204)

        val findUser = userReader.read(savedUser.id)

        // then
        assertThat(findUser.isDeleted).isTrue()
    }
}
