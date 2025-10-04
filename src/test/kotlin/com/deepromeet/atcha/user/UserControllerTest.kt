package com.deepromeet.atcha.user

import com.deepromeet.atcha.app.application.AppVersionAppender
import com.deepromeet.atcha.app.domain.Platform
import com.deepromeet.atcha.shared.web.ApiResponse
import com.deepromeet.atcha.shared.web.token.JwtTokenGenerator
import com.deepromeet.atcha.support.BaseControllerTest
import com.deepromeet.atcha.support.fixture.UserFixture
import com.deepromeet.atcha.user.api.request.UserInfoUpdateRequest
import com.deepromeet.atcha.user.api.response.UserInfoResponse
import com.deepromeet.atcha.user.application.UserAppender
import com.deepromeet.atcha.user.application.UserReader
import com.deepromeet.atcha.user.domain.User
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
    private val jwtTokenGenerator: JwtTokenGenerator,
    @Autowired
    private val userReader: UserReader,
    @Autowired
    private val userAppender: UserAppender,
    @Autowired
    private val appVersionAppender: AppVersionAppender
) : BaseControllerTest() {
    var accessToken: String = ""
    var user: User = UserFixture.create(id = 0L)

    @BeforeEach
    fun issueToken() {
        user = userAppender.append(user)
        val generateToken = jwtTokenGenerator.generateTokens(user.id)
        accessToken = generateToken.accessToken
        appVersionAppender.createAppVersion(Platform.ANDROID, "test v1.0.0")
    }

    @Test
    fun `회원 정보 조회`() {
        val result =
            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .header("X-Platform", "ANDROID")
                .`when`().get("/api/members/me")
                .then().log().all()
                .statusCode(200)
                .extract().`as`(ApiResponse::class.java)
                .result
        val objectMapper = jacksonObjectMapper()
        val findUser: UserInfoResponse = objectMapper.convertValue(result, UserInfoResponse::class.java)
        assertThat(findUser.id).isEqualTo(user.id.value)
        assertThat(findUser.appVersion).isEqualTo("test v1.0.0")
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
        assertThat(findUser.alertFrequencies).isEqualTo(userInfoUpdateRequest.alertFrequencies)
        assertThat(findUser.homeAddress.address).isEqualTo(userInfoUpdateRequest.address)
        assertThat(findUser.homeAddress.coordinate.lat).isEqualTo(userInfoUpdateRequest.lat)
        assertThat(findUser.homeAddress.coordinate.lon).isEqualTo(userInfoUpdateRequest.lon)
        assertThat(findUser.fcmToken).isEqualTo(userInfoUpdateRequest.fcmToken)
    }

    @Test
    fun `회원 탈퇴`() {
        // given
        val request = mapOf("reason" to "서비스 불만")

        // when
        RestAssured.given()
            .log().all()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .delete("/api/members/me")
            .then()
            .log().all()
            .statusCode(204)

        assertThatThrownBy { userReader.read(user.id) }
            .isInstanceOf(UserException::class.java)
    }
}
