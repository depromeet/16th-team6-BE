package com.deepromeet.atcha.shared.infrastructure.circuitbreaker

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnCallNotPermittedEvent
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnErrorEvent
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnFailureRateExceededEvent
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnIgnoredErrorEvent
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnSlowCallRateExceededEvent
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnSuccessEvent
import io.github.resilience4j.feign.FeignDecorators
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CircuitBreakerConfiguration(
    private val circuitBreakerRegistry: CircuitBreakerRegistry
) {
    private val log = KotlinLogging.logger {}

    @Bean
    fun circuitBreakerFeignDecorators(): Map<CircuitBreakerType, FeignDecorators> {
        return CircuitBreakerType.entries.associateWith { type ->
            val circuitBreaker = circuitBreakerRegistry.circuitBreaker(type.instanceName)

            // 상태 전환 이벤트
            circuitBreaker.eventPublisher
                .onStateTransition { event: CircuitBreakerOnStateTransitionEvent ->
                    val metrics = circuitBreaker.metrics
                    log.info {
                        "🔄 [Circuit Breaker] ${type.instanceName} 상태 변경: " +
                            "${event.stateTransition.fromState} → ${event.stateTransition.toState} " +
                            "(실패율: ${metrics.failureRate}%, " +
                            "느린 호출율: ${metrics.slowCallRate}%)"
                    }
                }

            // 성공 이벤트
            circuitBreaker.eventPublisher
                .onSuccess { event: CircuitBreakerOnSuccessEvent ->
                    val metrics = circuitBreaker.metrics
                    log.debug {
                        "✅ [Circuit Breaker] ${type.instanceName} 호출 성공 " +
                            "(성공 호출 수: ${metrics.numberOfSuccessfulCalls}, " +
                            "총 호출 수: ${metrics.numberOfSuccessfulCalls + metrics.numberOfFailedCalls}, " +
                            "실패율: ${metrics.failureRate}%, " +
                            "상태: ${circuitBreaker.state})"
                    }
                }

            // 에러 이벤트
            circuitBreaker.eventPublisher
                .onError { event: CircuitBreakerOnErrorEvent ->
                    val metrics = circuitBreaker.metrics
                    log.warn {
                        "❌ [Circuit Breaker] ${type.instanceName} 호출 실패 " +
                            "(에러: ${event.throwable.javaClass.simpleName} " +
                            "실패 호출 수: ${metrics.numberOfFailedCalls}, " +
                            "총 호출 수: ${metrics.numberOfSuccessfulCalls + metrics.numberOfFailedCalls}, " +
                            "실패율: ${metrics.failureRate}%, " +
                            "상태: ${circuitBreaker.state})"
                    }
                }

            // 호출 차단 이벤트
            circuitBreaker.eventPublisher
                .onCallNotPermitted { event: CircuitBreakerOnCallNotPermittedEvent ->
                    val metrics = circuitBreaker.metrics
                    log.warn {
                        "🚫 [Circuit Breaker] ${type.instanceName} 호출 차단됨 " +
                            "(상태: ${circuitBreaker.state}, " +
                            "차단된 호출 수: ${metrics.numberOfNotPermittedCalls}, " +
                            "실패율: ${metrics.failureRate}%, " +
                            "느린 호출율: ${metrics.slowCallRate}%)"
                    }
                }

            // 실패율 임계값 초과 이벤트
            circuitBreaker.eventPublisher
                .onFailureRateExceeded { event: CircuitBreakerOnFailureRateExceededEvent ->
                    val metrics = circuitBreaker.metrics
                    val config = circuitBreaker.circuitBreakerConfig
                    log.error {
                        "🔥 [Circuit Breaker] ${type.instanceName} 실패율 임계값 초과! " +
                            "(현재 실패율: ${event.failureRate}%, " +
                            "임계값: ${config.failureRateThreshold}%, " +
                            "총 호출 수: ${metrics.numberOfSuccessfulCalls + metrics.numberOfFailedCalls}, " +
                            "실패 호출 수: ${metrics.numberOfFailedCalls})"
                    }
                }

            // 느린 호출율 임계값 초과 이벤트
            circuitBreaker.eventPublisher
                .onSlowCallRateExceeded { event: CircuitBreakerOnSlowCallRateExceededEvent ->
                    val metrics = circuitBreaker.metrics
                    val config = circuitBreaker.circuitBreakerConfig
                    log.error {
                        "🐌 [Circuit Breaker] ${type.instanceName} 느린 호출율 임계값 초과! " +
                            "(현재 느린 호출율: ${event.slowCallRate}%, " +
                            "임계값: ${config.slowCallRateThreshold}%, " +
                            "느린 호출 수: ${metrics.numberOfSlowCalls}, " +
                            "느린 호출 임계시간: ${config.slowCallDurationThreshold.toMillis()}ms)"
                    }
                }

            // 무시된 에러 이벤트
            circuitBreaker.eventPublisher
                .onIgnoredError { event: CircuitBreakerOnIgnoredErrorEvent ->
                    log.debug {
                        "ℹ️ [Circuit Breaker] ${type.instanceName} 에러 무시됨 " +
                            "(에러: ${event.throwable.javaClass.simpleName}: ${event.throwable.message})"
                    }
                }

            FeignDecorators.builder()
                .withCircuitBreaker(circuitBreaker)
                .build()
        }
    }
}
