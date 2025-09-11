package com.deepromeet.atcha.location.infrastructure.client.config

import com.deepromeet.atcha.location.infrastructure.client.TMapReverseGeoHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class TMapReverseGeoHttpClientConfig {
    @Bean
    fun tMapReverseGeoHttpClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${tmap.api.url}") baseUrl: String,
        @Value("\${tmap.api.app-key}") apiKey: String
    ): TMapReverseGeoHttpClient {
        val webClient =
            webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("appKey", apiKey)
                .build()

        val factory =
            HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build()

        return factory.createClient(TMapReverseGeoHttpClient::class.java)
    }
}
