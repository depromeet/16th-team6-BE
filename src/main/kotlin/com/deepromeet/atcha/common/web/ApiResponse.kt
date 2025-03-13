package com.deepromeet.atcha.common.web

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val responseCode: String,
    val timeStamp: LocalDateTime? = null,
    val path: String? = null,
    val message: String? = null,
    val result: T? = null
) {
    companion object {
        fun <T> success(result: T): ApiResponse<T> = ApiResponse(responseCode = "SUCCESS", result = result)

        fun error(
            errorCode: String,
            path: String,
            message: String
        ): ApiResponse<Unit> =
            ApiResponse(
                path = path,
                timeStamp = LocalDateTime.now(),
                responseCode = errorCode,
                message = message
            )

        fun error(
            errorCode: String,
            message: String
        ): ApiResponse<Unit> =
            ApiResponse(
                responseCode = errorCode,
                timeStamp = LocalDateTime.now(),
                message = message
            )
    }
}
