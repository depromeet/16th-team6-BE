package com.deepromeet.atcha.auth.infrastructure.provider.apple

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.fallback.CircuitBreakerFallbackFactory
import feign.Feign
import io.github.resilience4j.feign.FeignDecorators
import io.github.resilience4j.feign.Resilience4jFeign
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

class AppleFeignConfig(
    private val circuitBreakerDecorators: Map<CircuitBreakerType, FeignDecorators>
) {
    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun circuitBreakerDecorator(): Feign.Builder {
        val decorator = circuitBreakerDecorators[CircuitBreakerType.AUTH_API]!!
        return Feign.builder()
            .addCapability(Resilience4jFeign.capability(decorator))
    }

    @Bean
    fun fallbackFactory(): CircuitBreakerFallbackFactory<Any> = CircuitBreakerFallbackFactory()
}
