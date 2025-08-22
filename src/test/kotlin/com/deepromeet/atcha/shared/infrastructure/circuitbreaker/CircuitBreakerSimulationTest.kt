package com.deepromeet.atcha.shared.infrastructure.circuitbreaker

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
class CircuitBreakerSimulationTest {
    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @Test
    fun `서킷 브레이커가 실패 임계치 도달 시 OPEN 상태로 변경되어야 한다`() {
        // given
        val circuitBreaker =
            circuitBreakerRegistry.circuitBreaker("test-circuit-breaker") {
                CircuitBreakerConfig.custom()
                    .failureRateThreshold(75.0f) // 75% 실패율로 설정
                    .minimumNumberOfCalls(4)
                    .slidingWindowSize(4)
                    .build()
            }

        // when - 처음 3번 실패 (아직 최소 호출 수 미달)
        repeat(3) {
            try {
                circuitBreaker.executeSupplier { throw RuntimeException("Simulated failure") }
            } catch (e: Exception) {
                // 예외 무시
            }
        }

        // then - 아직 최소 호출 수에 도달하지 않아 CLOSED 상태 유지
        assertEquals(State.CLOSED, circuitBreaker.state)

        // when - 4번째 호출도 실패 (4번 중 4번 실패 = 100% > 75%)
        try {
            circuitBreaker.executeSupplier { throw RuntimeException("Simulated failure") }
        } catch (e: Exception) {
            // 예외 무시
        }

        // then - 이제 서킷 브레이커가 열려야 함
        assertEquals(State.OPEN, circuitBreaker.state)
    }

    @Test
    fun `OPEN 상태의 서킷 브레이커는 추가 호출을 차단해야 한다`() {
        // given
        val circuitBreaker =
            circuitBreakerRegistry.circuitBreaker("test-blocking-circuit-breaker") {
                CircuitBreakerConfig.custom()
                    .failureRateThreshold(50.0f)
                    .minimumNumberOfCalls(2)
                    .slidingWindowSize(2)
                    .build()
            }

        // when - 서킷 브레이커를 OPEN 상태로 만들기
        repeat(2) {
            try {
                circuitBreaker.executeSupplier { throw RuntimeException("Simulated failure") }
            } catch (e: Exception) {
                // 예외 무시
            }
        }

        assertEquals(State.OPEN, circuitBreaker.state)

        // when - OPEN 상태에서 호출 시도
        var callBlocked = false
        try {
            circuitBreaker.executeSupplier { "should be blocked" }
        } catch (e: CallNotPermittedException) {
            callBlocked = true
        }

        // then
        assertEquals(true, callBlocked)
    }
}
