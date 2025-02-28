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
    FAILED_TO_READ_POIS(500, "LOCATION_001", "POI 정보를 읽어오는데 실패했습니다", LogLevel.ERROR)
}

sealed class LocationException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object FailedToReadPOIs : LocationException(LocationErrorType.FAILED_TO_READ_POIS) {
        override fun readResolve(): Any = FailedToReadPOIs
    }
}
