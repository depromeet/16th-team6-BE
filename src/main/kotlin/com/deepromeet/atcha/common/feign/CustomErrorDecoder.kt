package com.deepromeet.atcha.common.feign

import feign.Response
import feign.codec.ErrorDecoder

class CustomErrorDecoder : ErrorDecoder {
    override fun decode(
        methodKey: String,
        response: Response,
    ): Exception {
        return when (response.status()) {
            400 -> FeignException.ExternalApiBadRequestError
            404 -> FeignException.ExternalApiNotFoundError
            500 -> FeignException.ExternalApiInternalServerError
            else -> {
                RuntimeException("외부 API에서 알 수 없는 오류가 발생했습니다 - " + response.reason())
            }
        }
    }
}
