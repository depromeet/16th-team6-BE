package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.WebClientCircuitBreakerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class KakaoHttpClientConfig(
    @Value("\${kakao.api.url}") private val kakaoApiUrl: String,
    private val circuitBreakerFactory: WebClientCircuitBreakerFactory
) {
    @Bean
    fun kakaoWebClient(commonWebClient: WebClient): WebClient {
        return commonWebClient.mutate()
            .baseUrl(kakaoApiUrl)
            .defaultHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.AUTH_API, "Kakao"))
            .build()
    }

    @Bean
    fun kakaoHttpClient(kakaoWebClient: WebClient): KakaoHttpClient {
        val adapter = WebClientAdapter.create(kakaoWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(KakaoHttpClient::class.java)
    }
}
