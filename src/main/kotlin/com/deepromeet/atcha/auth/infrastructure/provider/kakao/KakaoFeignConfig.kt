package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import feign.Feign
import feign.RequestInterceptor
import io.github.resilience4j.feign.FeignDecorators
import io.github.resilience4j.feign.Resilience4jFeign
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

class KakaoFeignConfig(
    private val circuitBreakerDecorators: Map<CircuitBreakerType, FeignDecorators>
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate ->
            requestTemplate.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        }
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun circuitBreakerDecorator(): Feign.Builder {
        val decorator = circuitBreakerDecorators[CircuitBreakerType.AUTH_API]!!
        return Feign.builder()
            .addCapability(Resilience4jFeign.capability(decorator))
    }
}
