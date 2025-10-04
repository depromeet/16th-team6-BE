package com.deepromeet.atcha.app

import com.deepromeet.atcha.app.api.request.AppVersionUpdateRequest
import com.deepromeet.atcha.shared.web.ApiResponse
import com.deepromeet.atcha.support.BaseControllerTest
import io.restassured.RestAssured
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class AppControllerTest : BaseControllerTest() {
    @Test
    fun `앱 버전을 추가한다`() {
        // given
        val request = AppVersionUpdateRequest("test v1.0.0")

        // when & then
        RestAssured.given().log().all()
            .header("Content-Type", "application/json")
            .header("X-Platform", "ANDROID")
            .body(request)
            .`when`().post("/api/app/version")
            .then().log().all()
            .statusCode(204)
    }

    @Test
    fun `앱 버전을 조회한다`() {
        // given
        val version = "test v1.0.0"
        val request = AppVersionUpdateRequest(version)

        RestAssured.given().log().all()
            .header("Content-Type", "application/json")
            .header("X-Platform", "ANDROID")
            .body(request)
            .`when`().post("/api/app/version")
            .then().log().all()
            .statusCode(204)
        // when & then

        val result =
            RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("X-Platform", "ANDROID")
                .`when`().get("/api/app/version")
                .then().log().all()
                .statusCode(200)
                .extract().`as`(ApiResponse::class.java)
                .result

        Assertions.assertThat(result).isEqualTo(version)
    }
}
