package com.deepromeet.atcha.common.feign

import feign.Response
import feign.codec.ErrorDecoder

class CustomErrorDecoder : ErrorDecoder {
    override fun decode(
        methodKey: String,
        response: Response
    ): Exception {
        return when (response.status()) {
            400 -> FeignException.ExternalApiBadRequestError
            403 -> FeignException.ExternalApiForbiddenError
            404 -> FeignException.ExternalApiNotFoundError
            500 -> FeignException.ExternalApiInternalServerError
            else -> FeignException.ExternalApiUnknownError
        }
    }
}
