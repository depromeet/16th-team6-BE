package com.deepromeet.atcha.shared.infrastructure.circuitbreaker

import io.github.resilience4j.circuitbreaker.CircuitBreaker.State
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
class CircuitBreakerIntegrationTest {
    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @Test
    fun `서킷 브레이커 인스턴스들이 올바르게 등록되어야 한다`() {
        // given
        val expectedCircuitBreakers = listOf("public-api", "commercial-api", "auth-api")

        // when & then
        expectedCircuitBreakers.forEach { name ->
            val circuitBreaker = circuitBreakerRegistry.circuitBreaker(name)
            assertNotNull(circuitBreaker)
            assertEquals(State.CLOSED, circuitBreaker.state)
        }
    }

    @Test
    fun `공공 API 서킷 브레이커 설정이 올바르게 적용되어야 한다`() {
        // given
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker("public-api")

        // when & then
        val config = circuitBreaker.circuitBreakerConfig
        println("Public API - FailureRateThreshold: ${config.failureRateThreshold}")
        println("Public API - MinimumNumberOfCalls: ${config.minimumNumberOfCalls}")
        println("Public API - SlidingWindowSize: ${config.slidingWindowSize}")

        assertEquals(60.0f, config.failureRateThreshold)
        assertEquals(10, config.minimumNumberOfCalls)
        assertEquals(20, config.slidingWindowSize)
    }

    @Test
    fun `상용 API 서킷 브레이커 설정이 올바르게 적용되어야 한다`() {
        // given
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker("commercial-api")

        // when & then
        val config = circuitBreaker.circuitBreakerConfig
        assertEquals(50.0f, config.failureRateThreshold)
        assertEquals(5, config.minimumNumberOfCalls)
        assertEquals(10, config.slidingWindowSize)
    }

    @Test
    fun `인증 API 서킷 브레이커 설정이 올바르게 적용되어야 한다`() {
        // given
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker("auth-api")

        // when & then
        val config = circuitBreaker.circuitBreakerConfig
        assertEquals(40.0f, config.failureRateThreshold)
        assertEquals(3, config.minimumNumberOfCalls)
        assertEquals(8, config.slidingWindowSize)
    }
}
