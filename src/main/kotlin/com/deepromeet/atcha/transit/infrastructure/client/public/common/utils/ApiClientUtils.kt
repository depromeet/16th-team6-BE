package com.deepromeet.atcha.transit.infrastructure.client.public.common.utils

import com.deepromeet.atcha.shared.exception.ExternalApiError
import com.deepromeet.atcha.shared.exception.ExternalApiException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.PublicGyeonggiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import kotlin.coroutines.cancellation.CancellationException

private val log = KotlinLogging.logger {}

@Component
object ApiClientUtils {
    suspend fun <T, R> callApiByKeyProvider(
        keyProvider: () -> String,
        apiCall: (String) -> T,
        processResult: (T) -> R,
        errorMessage: String
    ): R {
        val response: T =
            try {
                val apiKey = keyProvider()
                interruptible { apiCall(apiKey) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: CallNotPermittedException) {
                log.warn { "서킷 브레이커로 인해 호출 차단됨 - $errorMessage" }
                throw ExternalApiException.of(ExternalApiError.EXTERNAL_API_CIRCUIT_BREAKER_OPEN, e)
            } catch (e: Exception) {
                log.warn(e) { "API 호출 중 오류: ${e.message} - $errorMessage" }
                throw ExternalApiException.of(ExternalApiError.EXTERNAL_API_UNKNOWN_ERROR, e)
            }
        return processResult(response)
    }

    suspend fun <T, R> callApiWithRetry(
        primaryKey: String,
        spareKey: String,
        realLastKey: String,
        apiCall: (String) -> T,
        isLimitExceeded: (T) -> Boolean,
        processResult: (T) -> R,
        errorMessage: String
    ): R {
        val keys = listOf(primaryKey, spareKey, realLastKey)
        val successful = callApiWithRetryInternal(keys, apiCall, isLimitExceeded, errorMessage, 0)
        return processResult(successful)
    }

    private suspend fun <T> callApiWithRetryInternal(
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
                interruptible { apiCall(currentKey) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: CallNotPermittedException) {
                log.warn { "서킷 브레이커로 인해 호출 차단됨 - $errorMessage" }
                throw ExternalApiException.of(ExternalApiError.EXTERNAL_API_CIRCUIT_BREAKER_OPEN, e)
            } catch (e: Exception) {
                log.warn(e) { "예상치 못한 오류: ${e.message} - $errorMessage" }
                throw ExternalApiException.of(ExternalApiError.EXTERNAL_API_UNKNOWN_ERROR, e)
            }

        return if (isLimitExceeded(response)) {
            log.warn { "API 키(${index + 1}) 제한, 다음 키 재시도." }
            callApiWithRetryInternal(keys, apiCall, isLimitExceeded, errorMessage, index + 1)
        } else {
            response
        }
    }

    private suspend fun <T> interruptible(block: () -> T): T =
        withContext(Dispatchers.IO) { runInterruptible { block() } }

    fun <T> isServiceResultApiLimitExceeded(response: ServiceResult<T>): Boolean {
        val limitMessages =
            listOf(
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR"
            )
        val limited =
            response.msgHeader.headerCd == 7 ||
                limitMessages.any { response.msgHeader.headerMsg?.contains(it) == true }
        if (limited) log.warn { "공공 API 요청 수 초과: ${response.msgHeader.headerMsg}" }
        return limited
    }

    fun <T> isGyeonggiApiLimitExceeded(response: PublicGyeonggiResponse<T>): Boolean =
        response.msgBody == null && !response.msgHeader.isEmptyResponse()
}
