package com.deepromeet.atcha.location.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class LocationErrorType(
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

sealed class LocationException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object FailedToReadPOIs : LocationException(LocationErrorType.FAILED_TO_READ_POIS) {
        override fun readResolve(): Any = FailedToReadPOIs
    }

    data object FailedToReadLocation : LocationException(LocationErrorType.FAILED_TO_READ_LOCATION) {
        override fun readResolve(): Any = FailedToReadLocation
    }

    data object InvalidLatitude : LocationException(LocationErrorType.INVALID_LATITUDE) {
        override fun readResolve(): Any = InvalidLatitude
    }

    data object InvalidLongitude : LocationException(LocationErrorType.INVALID_LONGITUDE) {
        override fun readResolve(): Any = InvalidLongitude
    }
}
