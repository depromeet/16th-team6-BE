package com.deepromeet.atcha.location.infrastructure.client.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import feign.Feign
import feign.RequestInterceptor
import io.github.resilience4j.feign.FeignDecorators
import io.github.resilience4j.feign.Resilience4jFeign
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

class TMapFeignConfig(
    @Value("\${tmap.api.app-key}")
    private val appKey: String,
    private val circuitBreakerDecorators: Map<CircuitBreakerType, FeignDecorators>
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate ->
            requestTemplate.header("appKey", appKey)
                .header("accept", "application/json")
                .header("content-type", "application/json")
        }
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun circuitBreakerDecorator(): Feign.Builder {
        val decorator = circuitBreakerDecorators[CircuitBreakerType.COMMERCIAL_API]!!
        return Feign.builder()
            .addCapability(Resilience4jFeign.capability(decorator))
    }
}
