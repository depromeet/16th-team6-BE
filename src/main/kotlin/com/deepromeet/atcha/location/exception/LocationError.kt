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
    LOCATION_API_ERROR(500, "MAP_001", "지도 API 호출 중 에러가 발생했습니다", LogLevel.ERROR)
}

sealed class LocationException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object LocationApiError : LocationException(LocationErrorType.LOCATION_API_ERROR) {
        override fun readResolve(): Any = LocationApiError
    }
}
