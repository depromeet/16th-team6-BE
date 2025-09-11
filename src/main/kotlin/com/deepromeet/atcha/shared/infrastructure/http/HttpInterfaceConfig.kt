package com.deepromeet.atcha.shared.infrastructure.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
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

        // XML 매퍼 설정
        val xmlMapper =
            XmlMapper().apply {
                findAndRegisterModules()
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }

        val strategies =
            ExchangeStrategies.builder().codecs { cfg ->
                cfg.defaultCodecs().maxInMemorySize(1 * 1_024 * 1_024)

                cfg.customCodecs().register(
                    Jackson2JsonDecoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML)
                )
                cfg.customCodecs().register(
                    Jackson2JsonEncoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML)
                )
            }.build()

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .filter(DecodingErrorLogger.logOnDecodingError())
            .filter { request, next ->
                next.exchange(request)
                    .onErrorMap(WebClientResponseException::class.java) { ex ->
                        HttpErrorHandler.handleException(ex)
                    }
            }
    }

    @Bean
    fun commonWebClient(webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder
            .build()
    }
}
