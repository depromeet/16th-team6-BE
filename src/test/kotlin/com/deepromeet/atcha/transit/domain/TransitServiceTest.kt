package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.infrastructure.client.response.TMapRouteResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

private val log = KotlinLogging.logger {}

@SpringBootTest
@ActiveProfiles("local")
class TransitServiceTest(
    @Autowired
    private val transitService: TransitService,
) {
    @Test
    fun `TMAP API 응답이 정상이다`() {
        // When: API 호출
        val response: TMapRouteResponse = transitService.getRoutes()

        // Then: 응답 검증
        assertNotNull(response)
        assertNotNull(response.metaData)
        assertNotNull(response.metaData.plan)
        assert(response.metaData.plan.itineraries.isNotEmpty()) { "경로 정보가 비어 있으면 안 됨" }

        log.info { "response: $response" }
    }
}
