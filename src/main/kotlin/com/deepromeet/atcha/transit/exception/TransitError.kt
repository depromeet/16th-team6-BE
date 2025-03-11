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
    INVALID_BUS_ROUTE(400, "TRS_003", "유효하지 않은 버스 노선입니다", LogLevel.ERROR),
    INVALID_BUS_STATION(400, "TRS_004", "유효하지 않은 버스 정류장입니다", LogLevel.ERROR),
    NOT_FOUND_BUS_ARRIVAL(404, "TRS_005", "버스 도착 정보를 찾을 수 없습니다", LogLevel.ERROR),
    NOT_FOUND_SUBWAY_STATION(404, "TRS_006", "지하철 역을 찾을 수 없습니다", LogLevel.ERROR),
    NOT_FOUND_SUBWAY_TIME_TABLE(404, "TRS_007", "지하철 시간표를 찾을 수 없습니다", LogLevel.ERROR),
    SUBWAY_DIRECTION_RESOLVE_FAILED(400, "TRS_008", "유효하지 않은 지하철 방향입니다", LogLevel.ERROR),
    NOT_FOUND_SUBWAY_ROUTE(404, "TRS_009", "지하철 노선을 찾을 수 없습니다", LogLevel.ERROR),
    NOT_FOUND_BUS_TIME(404, "TRS_010", "버스 시간표를 찾을 수 없습니다", LogLevel.ERROR)
}

sealed class TransitException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object TaxiFareFetchFailed : TransitException(TransitErrorType.TAXI_FARE_FETCH_FAILED) {
        override fun readResolve(): Any = TaxiFareFetchFailed
    }

    data object NotFoundBusRoute : TransitException(TransitErrorType.INVALID_BUS_ROUTE) {
        override fun readResolve(): Any = NotFoundBusRoute
    }

    data object NotFoundBusStation : TransitException(TransitErrorType.INVALID_BUS_STATION) {
        override fun readResolve(): Any = NotFoundBusStation
    }

    data object NotFoundBusArrival : TransitException(TransitErrorType.NOT_FOUND_BUS_ARRIVAL) {
        override fun readResolve(): Any = NotFoundBusArrival
    }

    data object NotFoundBusTime : TransitException(TransitErrorType.NOT_FOUND_BUS_TIME) {
        override fun readResolve(): Any = NotFoundBusTime
    }

    data object NotFoundSubwayStation : TransitException(TransitErrorType.NOT_FOUND_SUBWAY_STATION) {
        override fun readResolve(): Any = NotFoundSubwayStation
    }

    data object NotFoundSubwayTimeTable : TransitException(TransitErrorType.NOT_FOUND_SUBWAY_TIME_TABLE) {
        override fun readResolve(): Any = NotFoundSubwayTimeTable
    }

    data object SubwayDirectionResolveFailed : TransitException(TransitErrorType.SUBWAY_DIRECTION_RESOLVE_FAILED) {
        override fun readResolve(): Any = SubwayDirectionResolveFailed
    }

    data object NotFoundSubwayRoute : TransitException(TransitErrorType.NOT_FOUND_SUBWAY_ROUTE) {
        override fun readResolve(): Any = NotFoundSubwayRoute
    }
}
