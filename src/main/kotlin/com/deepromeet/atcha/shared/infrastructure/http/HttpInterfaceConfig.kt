package com.deepromeet.atcha.shared.infrastructure.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.github.oshai.kotlinlogging.KotlinLogging
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
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class HttpInterfaceConfig {
    private val log = KotlinLogging.logger {}

    @Bean
    fun customWebClientBuilder(): WebClient.Builder {
        val httpClient =
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1_000)
                .responseTimeout(Duration.ofMillis(5_000))
                .doOnConnected { conn ->
                    conn.addHandlerLast(ReadTimeoutHandler(5_000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(WriteTimeoutHandler(5_000, TimeUnit.MILLISECONDS))
                }

        val xmlMapper =
            XmlMapper().apply {
                findAndRegisterModules()
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }

        val strategies =
            ExchangeStrategies.builder().codecs { cfg ->
                cfg.defaultCodecs().maxInMemorySize(2 * 1_024 * 1_024)
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
            .filter { request, next ->
                next.exchange(request)
                    .retryWhen(createRetrySpec())
                    .flatMap { resp ->
                        if (resp.statusCode().isError) {
                            resp.createException().flatMap { ex ->
                                Mono.error(HttpErrorHandler.handleException(ex))
                            }
                        } else {
                            Mono.just(resp)
                        }
                    }
            }
            .filter(DecodingErrorLogger.logOnDecodingError())
    }

    private fun createRetrySpec(): Retry {
        return Retry.backoff(3, Duration.ofMillis(500)) // 3회 재시도, 500ms 간격으로 exponential backoff
            .maxBackoff(Duration.ofSeconds(2))
            .filter(::shouldRetry)
            .doBeforeRetry { retrySignal ->
                log.warn("재시도 시도: ${retrySignal.totalRetries() + 1}회, 예외: ${retrySignal.failure().message}")
            }
    }

    private fun shouldRetry(throwable: Throwable): Boolean {
        return when {
            throwable is WebClientRequestException -> true
            throwable is WebClientResponseException -> throwable.statusCode.is5xxServerError
            else -> false
        }
    }

    @Bean
    fun commonWebClient(customWebClientBuilder: WebClient.Builder): WebClient {
        return customWebClientBuilder
            .build()
    }
}
