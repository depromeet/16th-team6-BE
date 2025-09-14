package com.deepromeet.atcha.transit.infrastructure.client.kakao.config

import com.deepromeet.atcha.transit.infrastructure.client.kakao.KakaoRouteHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class KakaoRouteHttpClientConfig {
    @Bean
    fun kakaoRouteHttpClient(customWebClientBuilder: WebClient.Builder): KakaoRouteHttpClient {
        val webClient =
            customWebClientBuilder
                .baseUrl("https://app.map.kakao.com")
                .build()

        val factory =
            HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build()

        return factory.createClient(KakaoRouteHttpClient::class.java)
    }
}
