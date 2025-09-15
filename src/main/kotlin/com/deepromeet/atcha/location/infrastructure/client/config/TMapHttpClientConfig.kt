package com.deepromeet.atcha.location.infrastructure.client.config

import com.deepromeet.atcha.location.infrastructure.client.TMapLocationHttpClient
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.WebClientCircuitBreakerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class TMapHttpClientConfig(
    @Value("\${tmap.api.url}") private val tmapApiUrl: String,
    @Value("\${tmap.api.app-key}") private val appKey: String,
    private val circuitBreakerFactory: WebClientCircuitBreakerFactory
) {
    @Bean
    fun tmapWebClient(commonWebClient: WebClient): WebClient {
        return commonWebClient.mutate()
            .baseUrl(tmapApiUrl)
            .defaultHeader("appKey", appKey)
            .defaultHeader("accept", "application/json")
            .defaultHeader("content-type", "application/json")
            .filters { filters ->
                filters.add(
                    0,
                    circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.COMMERCIAL_API, "T-MAP")
                )
            }
            .build()
    }

    @Bean
    fun tmapLocationHttpClient(tmapWebClient: WebClient): TMapLocationHttpClient {
        val adapter = WebClientAdapter.create(tmapWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(TMapLocationHttpClient::class.java)
    }
}
