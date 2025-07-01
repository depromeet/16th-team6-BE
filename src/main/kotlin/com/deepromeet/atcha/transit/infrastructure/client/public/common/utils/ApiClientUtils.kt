package com.deepromeet.atcha.transit.infrastructure.client.public.common.utils

import com.deepromeet.atcha.common.feign.ExternalApiError
import com.deepromeet.atcha.common.feign.ExternalApiException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.PublicGyeonggiResponse
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

object ApiClientUtils {
    fun <T, R> callApiByKeyProvider(
        keyProvider: () -> String,
        apiCall: (String) -> T,
        processResult: (T) -> R,
        errorMessage: String
    ): R {
        val response =
            try {
                val apiKey = keyProvider()
                apiCall(apiKey)
            } catch (e: Exception) {
                log.warn(e) { "API 호출 중 예상치 못한 오류 발생: ${e.message} - $errorMessage" }
                throw ExternalApiException.of(ExternalApiError.EXTERNAL_API_UNKNOWN_ERROR, e)
            }
        return processResult(response)
    }

    fun <T, R> callApiWithRetry(
        primaryKey: String,
        spareKey: String,
        realLastKey: String,
        apiCall: (String) -> T,
        isLimitExceeded: (T) -> Boolean,
        processResult: (T) -> R,
        errorMessage: String
    ): R {
        val keys = listOf(primaryKey, spareKey, realLastKey)
        val successfulResponse = callApiWithRetryInternal(keys, apiCall, isLimitExceeded, errorMessage, 0)
        return processResult(successfulResponse)
    }

    private fun <T> callApiWithRetryInternal(
        keys: List<String>,
        apiCall: (String) -> T,
        isLimitExceeded: (T) -> Boolean,
        errorMessage: String,
        index: Int
    ): T {
        if (index >= keys.size) {
            log.warn { "모든 API 키가 실패했습니다. $errorMessage" }
            throw ExternalApiException.of(ExternalApiError.EXTERNAL_API_CALL_LIMIT_EXCEEDED, errorMessage)
        }

        val currentKey = keys[index]

        val response =
            try {
                apiCall(currentKey)
            } catch (e: Exception) {
                log.warn { "예상치 못한 오류 발생: ${e.message} - $errorMessage" }
                throw ExternalApiException.of(ExternalApiError.EXTERNAL_API_UNKNOWN_ERROR, e)
            }

        return if (isLimitExceeded(response)) {
            log.warn { "API 키(${index + 1}) 제한됨. 다음 키로 재시도합니다. $response" }
            callApiWithRetryInternal(keys, apiCall, isLimitExceeded, errorMessage, index + 1)
        } else {
            response
        }
    }

    fun <T> isServiceResultApiLimitExceeded(response: ServiceResult<T>): Boolean {
        val limitMessages =
            listOf(
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR",
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR"
            )
        val isLimited =
            response.msgHeader.headerCd == 7 ||
                limitMessages.any {
                    response.msgHeader.headerMsg?.contains(it)
                        ?: false
                }

        if (isLimited) {
            log.warn { "공공 API 요청 수 초과: ${response.msgHeader.headerMsg}" }
        }

        return isLimited
    }

    fun <T> isGyeonggiApiLimitExceeded(response: PublicGyeonggiResponse<T>): Boolean {
        return response.msgBody == null && !response.msgHeader.isEmptyResponse()
    }
}
