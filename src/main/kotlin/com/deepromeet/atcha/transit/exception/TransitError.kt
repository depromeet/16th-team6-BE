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
    NOT_FOUND_SUBWAY_STATION(404, "TRS_006", "지하철 역을 찾을 수 없습니다", LogLevel.ERROR),
    NOT_FOUND_SUBWAY_ROUTE(404, "TRS_009", "지하철 노선을 찾을 수 없습니다", LogLevel.ERROR),
    NOT_FOUND_TIME(404, "TRS_010", "시간표를 찾을 수 없습니다", LogLevel.ERROR),
    DISTANCE_TOO_SHORT(400, "TRS_011", "출발지와 도착지 간 거리가 너무 가깝습니다.", LogLevel.ERROR),
    SERVICE_AREA_NOT_SUPPORTED(400, "TRS_012", "서비스 지역이 아닙니다.", LogLevel.ERROR),
    NOT_FOUND_ROUTE(404, "TRS_013", "경로를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_POSITION(404, "TRS_014", "버스 위치를 찾을 수 없습니다.", LogLevel.ERROR),
    BUS_ROUTE_STATION_LIST_FETCH_FAILED(500, "TRS_015", "버스 노선 경유 정류소를 가져오는데 실패했습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_SCHEDULE(404, "TRS_016", "버스 도착 정보를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_REAL_TIME(404, "TRS_017", "버스 실시간 정보를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_OPERATION_INFO(404, "TRS_017", "버스 운행 정보를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_STATION(404, "TRS_018", "버스 정류소를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_ROUTE(404, "TRS_019", "버스 노선을 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_SUBWAY_LAST_TIME(404, "TRS_020", "지하철 막차 시간을 찾을 수 없습니다.", LogLevel.ERROR)
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

    data object NotFoundTime : TransitException(TransitErrorType.NOT_FOUND_TIME) {
        override fun readResolve(): Any = NotFoundTime
    }

    data object NotFoundSubwayStation : TransitException(TransitErrorType.NOT_FOUND_SUBWAY_STATION) {
        override fun readResolve(): Any = NotFoundSubwayStation
    }

    data object NotFoundSubwayRoute : TransitException(TransitErrorType.NOT_FOUND_SUBWAY_ROUTE) {
        override fun readResolve(): Any = NotFoundSubwayRoute
    }

    data object DistanceTooShort : TransitException(TransitErrorType.DISTANCE_TOO_SHORT) {
        override fun readResolve(): Any = DistanceTooShort
    }

    data object ServiceAreaNotSupported : TransitException(TransitErrorType.SERVICE_AREA_NOT_SUPPORTED) {
        override fun readResolve(): Any = ServiceAreaNotSupported
    }

    data object NotFoundRoute : TransitException(TransitErrorType.NOT_FOUND_ROUTE) {
        override fun readResolve(): Any = NotFoundRoute
    }

    data object NotFoundBusPosition : TransitException(TransitErrorType.NOT_FOUND_BUS_POSITION) {
        override fun readResolve(): Any = NotFoundBusPosition
    }

    data object BusRouteStationListFetchFailed : TransitException(
        TransitErrorType.BUS_ROUTE_STATION_LIST_FETCH_FAILED
    ) {
        override fun readResolve(): Any = BusRouteStationListFetchFailed
    }

    data object NotFoundBusArrival : TransitException(TransitErrorType.NOT_FOUND_BUS_SCHEDULE) {
        override fun readResolve(): Any = NotFoundBusArrival
    }

    data object BusRouteOperationInfoFetchFailed : TransitException(
        TransitErrorType.NOT_FOUND_BUS_OPERATION_INFO
    ) {
        override fun readResolve(): Any = BusRouteOperationInfoFetchFailed
    }

    data object NotFoundBusStation : TransitException(TransitErrorType.NOT_FOUND_BUS_STATION) {
        override fun readResolve(): Any = NotFoundBusStation
    }

    data object NotFoundBusRoute : TransitException(TransitErrorType.NOT_FOUND_BUS_ROUTE) {
        override fun readResolve(): Any = NotFoundBusRoute
    }

    data object NotFoundSubwayLastTime : TransitException(TransitErrorType.NOT_FOUND_SUBWAY_LAST_TIME) {
        override fun readResolve(): Any = NotFoundSubwayLastTime
    }

    data object NotFoundBusRealTime : TransitException(TransitErrorType.NOT_FOUND_BUS_REAL_TIME) {
        override fun readResolve(): Any = NotFoundBusRealTime
    }
}
