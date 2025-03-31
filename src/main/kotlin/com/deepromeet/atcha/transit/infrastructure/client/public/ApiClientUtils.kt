package com.deepromeet.atcha.transit.infrastructure.client.common

import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiApiResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * API 클라이언트 공통 유틸리티 클래스
 */
object ApiClientUtils {
    /**
     * API 호출과 재시도 로직을 처리하는 공통 함수
     *
     * @param primaryKey 기본 API 키
     * @param spareKey 예비 API 키
     * @param apiCall API 호출 함수
     * @param isLimitExceeded API 제한 초과 여부 확인 함수
     * @param processResult API 결과 처리 함수
     * @param errorMessage 오류 발생 시 기록할 메시지
     * @return 처리 결과
     */
    fun <T, R> callApiWithRetry(
        primaryKey: String,
        spareKey: String,
        apiCall: (String) -> T,
        isLimitExceeded: (T) -> Boolean,
        processResult: (T) -> R,
        errorMessage: String
    ): R? {
        try {
            // 기본 키로 API 호출
            val response = apiCall(primaryKey)

            // API 제한 확인
            if (isLimitExceeded(response)) {
                log.warn { "기본 API 키가 제한되어 예비 키로 재시도합니다." }

                // 예비 키로 재시도
                val retryResponse = apiCall(spareKey)
                if (isLimitExceeded(retryResponse)) {
                    log.error { "예비 API 키도 제한되었습니다. 요청을 처리할 수 없습니다." }
                    return null
                }

                return processResult(retryResponse)
            }

            return processResult(response)
        } catch (e: Exception) {
            log.warn(e) { errorMessage }
            return null
        }
    }

    fun <T> isSeoulApiLimitExceeded(response: ServiceResult<T>): Boolean {
        // 제한 메시지 목록 (필요시 확장 가능)
        val limitMessages =
            listOf(
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR",
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR"
            )

        // 헤더 코드가 7이고 메시지가 제한 메시지 중 하나를 포함하는지 확인
        val isLimited =
            response.msgHeader.headerCd == 7 ||
                limitMessages.any { response.msgHeader.headerMsg.contains(it) }

        if (isLimited) {
            log.warn { "공공 API 요청 수 초과: ${response.msgHeader.headerMsg}" }
        }

        return isLimited
    }

    fun <T> isGyeonggiApiLimitExceeded(response: PublicGyeonggiApiResponse<T>): Boolean {
        // 제한 메시지 목록 (필요시 확장 가능)
        val limitMessages =
            listOf(
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR",
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR"
            )

        // 결과 코드가 0이 아니고 메시지가 제한 메시지 중 하나를 포함하는지 확인
        val isLimited =
            response.response.msgHeader.resultCode != "00" &&
                limitMessages.any { response.response.msgHeader.resultMessage.contains(it) }

        if (isLimited) {
            log.warn { "경기도 공공 API 요청 수 초과: ${response.response.msgHeader.resultMessage}" }
        }

        return isLimited
    }
}
