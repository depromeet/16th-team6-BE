package com.deepromeet.atcha.location.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class LocationError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    FAILED_TO_READ_POIS(500, "LOC_001", "POI 정보를 읽어오는데 실패했습니다", LogLevel.ERROR),
    FAILED_TO_READ_LOCATION(500, "LOC_002", "위치 정보를 읽어오는데 실패했습니다", LogLevel.ERROR),
    INVALID_LATITUDE(400, "LOC_003", "유효하지 않은 위도입니다", LogLevel.ERROR),
    INVALID_LONGITUDE(400, "LOC_004", "유효하지 않은 경도입니다", LogLevel.ERROR)
}

class LocationException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): LocationException {
            return LocationException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): LocationException {
            return LocationException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): LocationException {
            return LocationException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): LocationException {
            return LocationException(errorType, customMessage = message, cause = cause)
        }
    }
}
