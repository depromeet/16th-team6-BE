package com.deepromeet.seulseul.auth.infrastructure.client

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean

class kakaoFeignConfig {
    @Bean
    fun requestInterceptor() : RequestInterceptor {
        return RequestInterceptor { requestTemplate ->
            requestTemplate.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        }
    }
}
