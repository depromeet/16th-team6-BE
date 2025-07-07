package com.deepromeet.atcha.transit.infrastructure.client.kakao.response

import com.deepromeet.atcha.transit.domain.Fare
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException

data class KakaoRouteResponse(
    val results: List<RouteResult>?,
    val status: KakaoStatus
) {
    fun firstTaxiFare(): Fare? {
        if (status.code == "START_NOT_FOUND") {
            throw TransitException.of(TransitError.TAXI_START_NOT_FOUND)
        }

        if (status.code != "SUCCESS") {
            throw TransitException.of(
                TransitError.TAXI_FARE_FETCH_FAILED,
                status.message ?: "택시비 조회에 실패했습니다."
            )
        }
        return results?.firstOrNull()?.summary?.fare?.taxi?.let { Fare(it) }
    }
}

data class RouteResult(
    val summary: RouteSummary
)

data class RouteSummary(
    val fare: KakaoFare
)

data class KakaoFare(
    val taxi: Int
)

data class KakaoStatus(
    val code: String,
    val message: String?
)
