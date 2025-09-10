package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class KakaoHttpClientConfig(
    @Value("\${kakao.api.url}") private val kakaoApiUrl: String
) {
    @Bean
    fun kakaoWebClient(webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder
            .baseUrl(kakaoApiUrl)
            .defaultHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
            .build()
    }

    @Bean
    fun kakaoHttpClient(kakaoWebClient: WebClient): KakaoHttpClient {
        val adapter = WebClientAdapter.create(kakaoWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(KakaoHttpClient::class.java)
    }
}
