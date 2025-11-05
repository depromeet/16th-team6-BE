package com.deepromeet.atcha.support

import com.deepromeet.atcha.auth.infrastructure.provider.kakao.KakaoHttpClient
import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ExtendWith(DatabaseCleanerExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "kakao.api.url=http://dummy",
        "jwt.access.secret=thisisfortestdGVzdEFjY2Vzc1NlY3JldEtleVZhbHVlMTIzNDU2Nzg=",
        "jwt.refresh.secret=thisisfortestddGVzdFJmZXNoU2VjcmV0S2V5VmFsdWUxMjM0NTY3OA"
    ]
)
abstract class BaseControllerTest {
    @LocalServerPort
    private val port: Int = 0

    @MockitoBean
    lateinit var kakaoHttpClient: KakaoHttpClient

    @BeforeEach
    fun setPort() {
        RestAssured.port = port
    }
}
