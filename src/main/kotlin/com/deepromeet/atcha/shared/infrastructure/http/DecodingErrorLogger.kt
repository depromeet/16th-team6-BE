package com.deepromeet.atcha.shared.infrastructure.http

import com.deepromeet.atcha.shared.utils.PrettyBodyFormatter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.codec.DecodingException
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.http.client.reactive.ClientHttpResponse
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyExtractor
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger {}

class DecodingErrorLogger {
    companion object {
        fun logOnDecodingError(): ExchangeFilterFunction {
            return ExchangeFilterFunction.ofResponseProcessor { response ->
                val factory = DefaultDataBufferFactory.sharedInstance
                DataBufferUtils.join(response.bodyToFlux(DataBuffer::class.java))
                    .defaultIfEmpty(factory.wrap(ByteArray(0)))
                    .map { dataBuffer ->
                        val bytes = ByteArray(dataBuffer.readableByteCount())
                        dataBuffer.read(bytes)
                        DataBufferUtils.release(dataBuffer)
                        LoggingOnDecodeErrorResponse(response, bytes)
                    }
            }
        }
    }
}

private class LoggingOnDecodeErrorResponse(
    private val originalResponse: ClientResponse,
    private val cachedBody: ByteArray
) : ClientResponse {
    private fun logCachedBody(error: Throwable) {
        val prettyBody = PrettyBodyFormatter.format(cachedBody, originalResponse.headers().asHttpHeaders())
        log.warn(error) { "응답 디코딩 실패! 문제가 된 원본 응답 본문:\n$prettyBody" }
    }

    private fun buildNewResponseWithCachedBody(): ClientResponse {
        val factory = DefaultDataBufferFactory.sharedInstance
        return ClientResponse.from(originalResponse)
            .body(if (cachedBody.isEmpty()) Flux.empty() else Flux.just(factory.wrap(cachedBody)))
            .build()
    }

    override fun <T : Any?> bodyToMono(elementClass: Class<out T?>): Mono<T> {
        return buildNewResponseWithCachedBody().bodyToMono(elementClass)
            .doOnError(DecodingException::class.java) { logCachedBody(it) }
    }

    override fun <T : Any?> bodyToMono(elementTypeRef: ParameterizedTypeReference<T>): Mono<T> {
        return buildNewResponseWithCachedBody().bodyToMono(elementTypeRef)
            .doOnError(DecodingException::class.java) { logCachedBody(it) }
    }

    override fun <T : Any?> bodyToFlux(elementClass: Class<out T?>): Flux<T> {
        return buildNewResponseWithCachedBody().bodyToFlux(elementClass)
            .doOnError(DecodingException::class.java) { logCachedBody(it) }
    }

    override fun <T : Any?> bodyToFlux(elementTypeRef: ParameterizedTypeReference<T>): Flux<T> {
        return buildNewResponseWithCachedBody().bodyToFlux(elementTypeRef)
            .doOnError(DecodingException::class.java) { logCachedBody(it) }
    }

    override fun statusCode(): HttpStatusCode = originalResponse.statusCode()

    override fun headers(): ClientResponse.Headers = originalResponse.headers()

    override fun cookies(): MultiValueMap<String, ResponseCookie> = originalResponse.cookies()

    override fun strategies(): ExchangeStrategies = originalResponse.strategies()

    override fun request(): HttpRequest = originalResponse.request()

    override fun <T : Any> body(extractor: BodyExtractor<T, in ClientHttpResponse>): T =
        originalResponse.body(
            extractor
        )

    override fun releaseBody(): Mono<Void> = originalResponse.releaseBody()

    override fun <T : Any?> toEntity(bodyClass: Class<T>): Mono<ResponseEntity<T>> =
        originalResponse.toEntity(
            bodyClass
        )

    override fun <T : Any?> toEntity(bodyTypeReference: ParameterizedTypeReference<T>): Mono<ResponseEntity<T>> =
        originalResponse.toEntity(
            bodyTypeReference
        )

    override fun <T : Any?> toEntityList(elementClass: Class<T>): Mono<ResponseEntity<List<T>>> =
        originalResponse.toEntityList(
            elementClass
        )

    override fun <T : Any?> toEntityList(elementTypeRef: ParameterizedTypeReference<T>): Mono<ResponseEntity<List<T>>> =
        originalResponse.toEntityList(
            elementTypeRef
        )

    override fun toBodilessEntity(): Mono<ResponseEntity<Void>> = originalResponse.toBodilessEntity()

    override fun createException(): Mono<WebClientResponseException> = originalResponse.createException()

    override fun <T : Any?> createError(): Mono<T> = originalResponse.createError()

    override fun logPrefix(): String = originalResponse.logPrefix()
}
