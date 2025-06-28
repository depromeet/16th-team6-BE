package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.common.exception.CustomException
import com.deepromeet.atcha.common.exception.InfrastructureError
import com.deepromeet.atcha.common.exception.InfrastructureException
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
import feign.codec.DecodeException
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

object ApiClientUtils {
    fun <T, R> callApiByKeyProvider(
        keyProvider: () -> String,
        apiCall: (String) -> T,
        processResult: (T) -> R,
        errorMessage: String
    ): R {
        return try {
            val apiKey = keyProvider()
            val response = apiCall(apiKey)
            processResult(response)
        } catch (e: Exception) {
            if (e is CustomException) {
                throw e // CustomException은 그대로 던져서 상위로 전달
            }
            log.warn(e) { "API 호출 중 예상치 못한 오류 발생: ${e.message} - $errorMessage" }
            throw InfrastructureException.of(InfrastructureError.EXTERNAL_API_ERROR, e)
        }
    }

    /**
     * API 호출과 재시도 로직을 처리하는 공통 함수
     *
     * @param primaryKey 기본 API 키
     * @param spareKey 예비 API 키
     * @param realLastKey 실제 마지막 API 키
     * @param apiCall API 호출 함수
     * @param isLimitExceeded API 제한 초과 여부 확인 함수
     * @param processResult API 결과 처리 함수
     * @param errorMessage 오류 발생 시 기록할 메시지
     * @return 처리 결과
     */
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
        return callApiWithRetryInternal(keys, apiCall, isLimitExceeded, processResult, errorMessage, 0)
    }

    private fun <T, R> callApiWithRetryInternal(
        keys: List<String>,
        apiCall: (String) -> T,
        isLimitExceeded: (T) -> Boolean,
        processResult: (T) -> R,
        errorMessage: String,
        index: Int
    ): R {
        if (index >= keys.size) {
            log.warn { "모든 API 키가 실패했습니다. $errorMessage" }
            throw InfrastructureException.of(InfrastructureError.EXTERNAL_API_CALL_LIMIT_EXCEEDED, errorMessage)
        }

        val currentKey = keys[index]

        return try {
            val response = apiCall(currentKey)
            if (isLimitExceeded(response)) {
                log.warn { "API 키(${index + 1}) 제한됨. 다음 키로 재시도합니다. $errorMessage" }
                callApiWithRetryInternal(keys, apiCall, isLimitExceeded, processResult, errorMessage, index + 1)
            } else {
                processResult(response)
            }
        } catch (e: DecodeException) {
            log.warn(e) { "DecodeException 발생. API 응답 포맷이 예상과 다릅니다. 다음 키로 재시도합니다." }
            throw InfrastructureException.of(InfrastructureError.EXTERNAL_API_ERROR, e)
        } catch (e: Exception) {
            if (e is CustomException) {
                throw e // CustomException은 그대로 던져서 상위로 전달
            }
            log.warn { "예상치 못한 오류 발생: ${e.message} - $errorMessage" }
            throw InfrastructureException.of(InfrastructureError.EXTERNAL_API_ERROR, e)
        }
    }

    fun <T> isServiceResultApiLimitExceeded(response: ServiceResult<T>): Boolean {
        // 제한 메시지 목록 (필요시 확장 가능)
        val limitMessages =
            listOf(
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR",
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR"
            )

        // 헤더 코드가 7이고 메시지가 제한 메시지 중 하나를 포함하는지 확인
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
