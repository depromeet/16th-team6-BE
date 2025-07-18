package com.deepromeet.atcha

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("prod")
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",

        "redis.host=localhost",
        "redis.port=6379",
        "spring.data.redis.password=",

        "management.endpoints.web.exposure.include=health"
    ]
)
class ProductionContextTest {
    @Test
    fun `운영 환경에서도 어플리케이션이 정상 실행된다`() {
        // 빈 충돌만 검증, 실제 DB/Redis 연결은 하지 않음
    }
}
