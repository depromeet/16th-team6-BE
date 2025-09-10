package com.deepromeet.atcha.shared.infrastructure.circuitbreaker

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Component
class WebClientCircuitBreakerFactory(
    private val cbRegistry: CircuitBreakerRegistry
) {
    private val log = KotlinLogging.logger {}
    private val listenerRegistered = ConcurrentHashMap.newKeySet<String>()

    fun createCircuitBreakerFilter(
        type: CircuitBreakerType,
        clientName: String
    ): ExchangeFilterFunction {
        // 1) 타입별 기본 설정 복제
        val baseCfg = cbRegistry
            .circuitBreaker(type.instanceName)
            .circuitBreakerConfig

        val cbName = "${type.instanceName}:$clientName"
        val cb: CircuitBreaker = cbRegistry.circuitBreaker(cbName, baseCfg)

        // 2) 이벤트 리스너 등록 (한 번만)
        registerEventListeners(cb, type, cbName)

        // 3) WebClient ExchangeFilterFunction 반환
        return ExchangeFilterFunction.ofResponseProcessor { response ->
            Mono.just(response)
                .transformDeferred(CircuitBreakerOperator.of(cb))
        }
    }

    fun getCircuitBreaker(
        type: CircuitBreakerType,
        clientName: String
    ): CircuitBreaker {
        val baseCfg = cbRegistry
            .circuitBreaker(type.instanceName)
            .circuitBreakerConfig

        val cbName = "${type.instanceName}:$clientName"
        val cb: CircuitBreaker = cbRegistry.circuitBreaker(cbName, baseCfg)

        registerEventListeners(cb, type, cbName)
        return cb
    }

    private fun registerEventListeners(
        cb: CircuitBreaker,
        type: CircuitBreakerType,
        cbName: String
    ) {
        if (listenerRegistered.add(cbName)) {
            val label = "[서킷 브레이커/${type.name}] $cbName"

            cb.eventPublisher
                .onStateTransition { e ->
                    val m = cb.metrics
                    log.info {
                        "🔄 $label 상태 변경: " +
                            "${e.stateTransition.fromState} → ${e.stateTransition.toState} " +
                            "(실패율: ${m.failureRate}%, 느린 호출율: ${m.slowCallRate}%)"
                    }
                }
                .onSuccess {
                    val m = cb.metrics
                    log.debug {
                        "✅ $label 호출 성공 " +
                            "(성공: ${m.numberOfSuccessfulCalls}, " +
                            "총: ${m.numberOfSuccessfulCalls + m.numberOfFailedCalls}, " +
                            "실패율: ${m.failureRate}%, 상태: ${cb.state})"
                    }
                }
                .onError { ev ->
                    val m = cb.metrics
                    log.debug {
                        "❌ $label 호출 실패 " +
                            "(에러: ${ev.throwable.javaClass.simpleName}, " +
                            "실패: ${m.numberOfFailedCalls}, " +
                            "총: ${m.numberOfSuccessfulCalls + m.numberOfFailedCalls}, " +
                            "실패율: ${m.failureRate}%, 상태: ${cb.state})"
                    }
                }
                .onCallNotPermitted {
                    val m = cb.metrics
                    log.error {
                        "🚫 $label 호출 차단됨 " +
                            "(상태: ${cb.state}, " +
                            "차단: ${m.numberOfNotPermittedCalls}, " +
                            "실패율: ${m.failureRate}%, " +
                            "느린 호출율: ${m.slowCallRate}%)"
                    }
                }
                .onFailureRateExceeded { ev ->
                    val m = cb.metrics
                    val cfg = cb.circuitBreakerConfig
                    log.error {
                        "🔥 $label 실패율 임계 초과! " +
                            "(현재: ${ev.failureRate}%, " +
                            "임계값: ${cfg.failureRateThreshold}%, " +
                            "총: ${m.numberOfSuccessfulCalls + m.numberOfFailedCalls}, " +
                            "실패: ${m.numberOfFailedCalls})"
                    }
                }
                .onSlowCallRateExceeded { ev ->
                    val m = cb.metrics
                    val cfg = cb.circuitBreakerConfig
                    log.error {
                        "🐌 $label 느린 호출율 임계 초과! " +
                            "(현재: ${ev.slowCallRate}%, " +
                            "임계값: ${cfg.slowCallRateThreshold}%, " +
                            "느린 호출 수: ${m.numberOfSlowCalls}, " +
                            "임계시간: ${cfg.slowCallDurationThreshold.toMillis()}ms)"
                    }
                }
                .onIgnoredError { ev ->
                    log.debug {
                        "ℹ️ $label 에러 무시됨 " +
                            "(에러: ${ev.throwable.javaClass.simpleName}: ${ev.throwable.message})"
                    }
                }
        }
    }
}