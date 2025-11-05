package com.deepromeet.atcha.route.infrastructure.client.tmap.config

import com.deepromeet.atcha.route.infrastructure.client.tmap.TMapRouteHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class TMapRouteHttpClientConfig(
    private val rateLimitFilter: TMapRateLimitFilter
) {
    @Bean
    fun tmapRouteHttpClient(tmapWebClient: WebClient): TMapRouteHttpClient {
        val tmapApiClient =
            tmapWebClient.mutate()
                .filter(rateLimitFilter.rateLimitFilter())
                .build()
        val adapter = WebClientAdapter.create(tmapApiClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(TMapRouteHttpClient::class.java)
    }
}
