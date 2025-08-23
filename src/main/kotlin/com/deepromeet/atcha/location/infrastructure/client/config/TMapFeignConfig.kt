package com.deepromeet.atcha.location.infrastructure.client.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.FeignDecoratorsFactory
import feign.Feign
import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

class TMapFeignConfig(
    @Value("\${tmap.api.app-key}")
    private val appKey: String,
    private val decoratorsFactory: FeignDecoratorsFactory
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
    fun tmapFeignBuilder(context: ApplicationContext): Feign.Builder {
        return decoratorsFactory.builder(CircuitBreakerType.AUTH_API, context.displayName)
    }
}
