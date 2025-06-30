package com.deepromeet.atcha.transit.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class TransitError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    TRANSIT_API_ERROR(500, "TRS_001", "대중교통 API 호출 중 에러가 발생했습니다", LogLevel.ERROR),
    TAXI_FARE_FETCH_FAILED(500, "TRS_002", "택시 요금 조회에 실패했습니다", LogLevel.ERROR),
    NOT_FOUND_SUBWAY_STATION(404, "TRS_006", "지하철 역을 찾을 수 없습니다", LogLevel.ERROR),
    NOT_FOUND_SUBWAY_ROUTE(404, "TRS_009", "지하철 노선을 찾을 수 없습니다", LogLevel.ERROR),
    DISTANCE_TOO_SHORT(400, "TRS_011", "출발지와 도착지 간 거리가 너무 가깝습니다.", LogLevel.ERROR),
    SERVICE_AREA_NOT_SUPPORTED(400, "TRS_012", "서비스 지역이 아닙니다.", LogLevel.ERROR),
    NOT_FOUND_ROUTE(404, "TRS_013", "경로를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_POSITION(404, "TRS_014", "버스 위치를 찾을 수 없습니다.", LogLevel.ERROR),
    BUS_ROUTE_STATION_LIST_FETCH_FAILED(500, "TRS_015", "버스 노선 경유 정류소를 가져오는데 실패했습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_SCHEDULE(404, "TRS_016", "버스 시간표를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_REAL_TIME(404, "TRS_017", "버스 실시간 정보를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_OPERATION_INFO(404, "TRS_017", "버스 운행 정보를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_STATION(404, "TRS_018", "버스 정류소를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_BUS_ROUTE(404, "TRS_019", "버스 노선을 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_SUBWAY_LAST_TIME(404, "TRS_020", "지하철 막차 시간을 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_SUBWAY_SCHEDULE(404, "TRS_022", "지하철 시간표를 찾을 수 없습니다.", LogLevel.ERROR),
    NOT_FOUND_SPECIFIED_TIME(404, "TRS_023", "특정 시간에 해당하는 대중교통을 찾을 수 없습니다.", LogLevel.ERROR),
    INVALID_TIME_FORMAT(400, "TRS_024", "유효하지 않은 시간 형식입니다.", LogLevel.ERROR),
    TMAP_TIME_OUT(500, "TRS_025", "TMap이 응답하지 않습니다.", LogLevel.ERROR)
}

class TransitException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): TransitException {
            return TransitException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): TransitException {
            return TransitException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): TransitException {
            return TransitException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): TransitException {
            return TransitException(errorType, customMessage = message, cause = cause)
        }
    }
}
