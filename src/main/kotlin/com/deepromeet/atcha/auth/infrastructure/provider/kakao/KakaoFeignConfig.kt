package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.FeignDecoratorsFactory
import feign.Feign
import feign.RequestInterceptor
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

class KakaoFeignConfig(
    private val decoratorsFactory: FeignDecoratorsFactory
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate ->
            requestTemplate.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        }
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun kakaoFeignBuilder(context: ApplicationContext): Feign.Builder {
        return decoratorsFactory.builder(CircuitBreakerType.AUTH_API, context.displayName)
    }
}
