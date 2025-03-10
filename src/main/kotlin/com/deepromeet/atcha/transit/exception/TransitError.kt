package com.deepromeet.atcha.transit.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class TransitErrorType(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    TRANSIT_API_ERROR(500, "TRS_001", "대중교통 API 호출 중 에러가 발생했습니다", LogLevel.ERROR),
    TAXI_FARE_FETCH_FAILED(500, "TRS_002", "택시 요금 조회에 실패했습니다", LogLevel.ERROR),
    DISTANCE_TOO_SHORT(400, "TRS_003", "출발지와 도착지 간 거리가 너무 가깝습니다.", LogLevel.ERROR),
    SERVICE_AREA_NOT_SUPPORTED(400, "TRS_004", "서비스 지역이 아닙니다.", LogLevel.ERROR)
}

sealed class TransitException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object TransitApiError : TransitException(TransitErrorType.TRANSIT_API_ERROR) {
        override fun readResolve(): Any = TransitApiError
    }

    data object TaxiFareFetchFailed : TransitException(TransitErrorType.TAXI_FARE_FETCH_FAILED) {
        override fun readResolve(): Any = TaxiFareFetchFailed
    }

    data object DistanceTooShort : TransitException(TransitErrorType.DISTANCE_TOO_SHORT) {
        override fun readResolve(): Any = DistanceTooShort
    }

    data object ServiceAreaNotSupported : TransitException(TransitErrorType.SERVICE_AREA_NOT_SUPPORTED) {
        override fun readResolve(): Any = ServiceAreaNotSupported
    }
}
