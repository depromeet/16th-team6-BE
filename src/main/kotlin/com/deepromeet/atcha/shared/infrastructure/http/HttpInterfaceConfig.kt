package com.deepromeet.atcha.shared.infrastructure.http

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class HttpInterfaceConfig {
    @Bean
    fun webClientBuilder(): WebClient.Builder {
        val httpClient =
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                .responseTimeout(Duration.ofMillis(3_500))
                .doOnConnected { conn ->
                    conn.addHandlerLast(ReadTimeoutHandler(3_500, TimeUnit.MILLISECONDS))
                        .addHandlerLast(WriteTimeoutHandler(2_000, TimeUnit.MILLISECONDS))
                }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(1024 * 1024) // 1MB
            }
    }

    @Bean
    fun defaultWebClient(webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder.build()
    }
}
