package com.deepromeet.atcha.route.infrastructure.client.tmap.config

import com.deepromeet.atcha.route.infrastructure.client.tmap.TMapRouteHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class TMapRouteHttpClientConfig {
    @Bean
    fun tMapRouteHttpClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${tmap.api.url}") baseUrl: String,
        @Value("\${tmap.api.app-key}") apiKey: String
    ): TMapRouteHttpClient {
        val webClient =
            webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("appKey", apiKey)
                .build()

        val factory =
            HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build()

        return factory.createClient(TMapRouteHttpClient::class.java)
    }
}
