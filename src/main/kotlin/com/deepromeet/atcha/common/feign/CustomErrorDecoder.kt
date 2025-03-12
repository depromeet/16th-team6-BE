package com.deepromeet.atcha.common.feign

import feign.Response
import feign.codec.ErrorDecoder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.log

private val log = KotlinLogging.logger {}

class CustomErrorDecoder : ErrorDecoder {
    override fun decode(
        methodKey: String,
        response: Response
    ): Exception {
        val responseBody =
            response.body()
                ?.asInputStream()
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: "No response body"
        log.error {
            "Feign error - status: ${response.status()}, reason: ${response.reason()}, body: $responseBody"
        }
        return when (response.status()) {
            400 -> FeignException.ExternalApiBadRequestError
            403 -> FeignException.ExternalApiForbiddenError
            404 -> FeignException.ExternalApiNotFoundError
            500 -> FeignException.ExternalApiInternalServerError
            else -> FeignException.ExternalApiUnknownError
        }
    }
}
