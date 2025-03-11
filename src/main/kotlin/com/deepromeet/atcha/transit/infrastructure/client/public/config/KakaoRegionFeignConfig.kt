package com.deepromeet.atcha.transit.infrastructure.client.public.config

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders

class KakaoRegionFeignConfig(
    @Value("\${kakao.local.key}")
    private val apiKey: String
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor =
        RequestInterceptor {
            it.header(HttpHeaders.AUTHORIZATION, "KakaoAK $apiKey")
        }
}
