package com.deepromeet.atcha.auth.infrastructure.provider.apple

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.WebClientCircuitBreakerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class AppleHttpClientConfig(
    @Value("\${apple.api.url}") private val appleApiUrl: String,
    private val circuitBreakerFactory: WebClientCircuitBreakerFactory
) {
    @Bean
    fun appleWebClient(commonWebClient: WebClient): WebClient {
        return commonWebClient.mutate()
            .baseUrl(appleApiUrl)
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.AUTH_API, "Apple"))
            .build()
    }

    @Bean
    fun appleHttpClient(appleWebClient: WebClient): AppleHttpClient {
        val adapter = WebClientAdapter.create(appleWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(AppleHttpClient::class.java)
    }
}
