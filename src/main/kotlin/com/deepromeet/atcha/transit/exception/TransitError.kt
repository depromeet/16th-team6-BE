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
    TAXI_FARE_FETCH_FAILED(500, "TRS_002", "택시 요금 조회에 실패했습니다", LogLevel.ERROR)
}

sealed class TransitException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object TaxiFareFetchFailed : TransitException(TransitErrorType.TAXI_FARE_FETCH_FAILED) {
        override fun readResolve(): Any = TaxiFareFetchFailed
    }
}
