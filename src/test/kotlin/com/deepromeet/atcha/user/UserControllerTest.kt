package com.deepromeet.atcha.user

import com.deepromeet.atcha.app.domain.AppVersionAppender
import com.deepromeet.atcha.common.token.TokenGenerator
import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.support.BaseControllerTest
import com.deepromeet.atcha.support.fixture.UserFixture
import com.deepromeet.atcha.user.api.request.UserInfoUpdateRequest
import com.deepromeet.atcha.user.api.response.UserInfoResponse
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserAppender
import com.deepromeet.atcha.user.domain.UserReader
import com.deepromeet.atcha.user.exception.UserException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    private val userAppender: UserAppender,
    @Autowired
    private val appVersionAppender: AppVersionAppender
) : BaseControllerTest() {
    var accessToken: String = ""
    var user: User = UserFixture.create()

    @BeforeEach
    fun issueToken() {
        user = userAppender.save(user)
        val generateToken = tokenGenerator.generateTokens(user.id)
        accessToken = generateToken.accessToken
        appVersionAppender.createAppVersion("test v1.0.0")
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
        assertThat(findUser.id).isEqualTo(user.id)
    }

    @Test
    fun `회원 정보 수정`() {
        // given
        val userInfoUpdateRequest =
            UserInfoUpdateRequest(
                nickname = "새로운 닉네임",
                alertFrequencies = mutableSetOf(2),
                profileImageUrl = "new",
                address = "new",
                lat = 37.99,
                lon = 127.99,
                fcmToken = "new"
            )

        // when
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .body(userInfoUpdateRequest)
            .`when`().put("/api/members/me")
            .then().log().all()
            .statusCode(200)

        val findUser = userReader.read(user.id)

        // then
        assertThat(findUser.nickname).isEqualTo(userInfoUpdateRequest.nickname)
        assertThat(findUser.alertFrequencies).isEqualTo(userInfoUpdateRequest.alertFrequencies)
        assertThat(findUser.profileImageUrl).isEqualTo(userInfoUpdateRequest.profileImageUrl)
        assertThat(findUser.address.address).isEqualTo(userInfoUpdateRequest.address)
        assertThat(findUser.address.lat).isEqualTo(userInfoUpdateRequest.lat)
        assertThat(findUser.address.lon).isEqualTo(userInfoUpdateRequest.lon)
        assertThat(findUser.fcmToken).isEqualTo(userInfoUpdateRequest.fcmToken)
    }

    @Test
    fun `회원 탈퇴`() {
        // given && when
        RestAssured.given().log().all()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .`when`().delete("/api/members/me")
            .then().log().all()
            .statusCode(204)

        // then
        assertThatThrownBy { userReader.read(user.id) }
            .isInstanceOf(UserException.UserNotFound::class.java)
    }
}
