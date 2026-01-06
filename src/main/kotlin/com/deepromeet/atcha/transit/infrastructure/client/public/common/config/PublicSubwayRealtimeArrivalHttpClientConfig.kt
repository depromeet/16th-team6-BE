package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.WebClientCircuitBreakerFactory
import com.deepromeet.atcha.transit.infrastructure.client.public.common.PublicSubwayRealtimeArrivalHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class PublicSubwayRealtimeArrivalHttpClientConfig(
    @Value("\${open-api.api.url.realtime-subway}") private val realtimeSubwayApiUrl: String,
    private val circuitBreakerFactory: WebClientCircuitBreakerFactory
) {
    @Bean
    fun publicRealtimeSubwayWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(realtimeSubwayApiUrl)
            .filters { filters ->
                filters.add(
                    0,
                    circuitBreakerFactory.createCircuitBreakerFilter(
                        CircuitBreakerType.PUBLIC_REALTIME_API,
                        "PublicRealtimeSubway"
                    )
                )
            }
            .build()
    }

    @Bean
    fun publicRealtimeSubwayHttpClient(
        publicRealtimeSubwayWebClient: WebClient
    ): PublicSubwayRealtimeArrivalHttpClient {
        val adapter = WebClientAdapter.create(publicRealtimeSubwayWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicSubwayRealtimeArrivalHttpClient::class.java)
    }
}
