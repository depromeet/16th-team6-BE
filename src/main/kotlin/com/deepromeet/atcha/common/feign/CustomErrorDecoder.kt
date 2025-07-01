package com.deepromeet.atcha.common.feign

import feign.Response
import feign.codec.ErrorDecoder
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class CustomErrorDecoder : ErrorDecoder {
    override fun decode(
        methodKey: String,
        response: Response
    ): Exception {
        val requestUrl = response.request().url()
        val requestMethod = response.request().httpMethod()

        val responseBody =
            runCatching {
                response.body()?.asInputStream()?.bufferedReader()?.use { it.readText() }
            }.getOrNull() ?: "응답 본문을 읽을 수 없습니다."

        val detailedMessage =
            """
            - 외부 API 호출 오류 -
            - 요청: ${requestMethod.name} $requestUrl
            - 응답 상태: ${response.status()} ${response.reason()}
            - 응답 본문: $responseBody
            """.trimIndent()

        return when (response.status()) {
            400 -> ExternalApiException.of(ExternalApiError.EXTERNAL_API_BAD_REQUEST_ERROR, detailedMessage)
            403 -> ExternalApiException.of(ExternalApiError.EXTERNAL_API_FORBIDDEN_ERROR, detailedMessage)
            404 -> ExternalApiException.of(ExternalApiError.EXTERNAL_API_NOT_FOUND_ERROR, detailedMessage)
            500, 502, 503 ->
                ExternalApiException.of(
                    ExternalApiError.EXTERNAL_API_INTERNAL_SERVER_ERROR,
                    detailedMessage
                )
            else -> {
                log.warn { "정의되지 않은 외부 API 오류 발생. $detailedMessage" }
                ExternalApiException.of(ExternalApiError.EXTERNAL_API_UNKNOWN_ERROR, detailedMessage)
            }
        }
    }
}
