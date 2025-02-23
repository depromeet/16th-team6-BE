package com.deepromeet.atcha.common.web

import com.deepromeet.atcha.common.exception.ErrorReason
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val responseCode: String,
    val timeStamp: LocalDateTime = LocalDateTime.now(),
    val path: String? = null,
    val message: String? = null,
    val result: T? = null,
) {
    companion object {
        fun <T> success(result: T): ApiResponse<T> = ApiResponse(responseCode = "SUCCESS", result = result)

        fun success(): ApiResponse<Unit> = ApiResponse(responseCode = "SUCCESS")

        fun success(message: String): ApiResponse<Unit> =
            ApiResponse(
                responseCode = "SUCCESS",
                message = message,
            )

        fun error(
            errorReason: ErrorReason,
            path: String,
            message: String,
        ): ApiResponse<Unit> =
            ApiResponse(
                path = path,
                responseCode = errorReason.errorCode,
                message = message,
            )

        fun error(
            errorCode: String,
            message: String,
        ): ApiResponse<Unit> =
            ApiResponse(
                responseCode = errorCode,
                message = message,
            )
    }
}
