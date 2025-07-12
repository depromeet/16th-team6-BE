package com.deepromeet.atcha.transit.infrastructure.client.kakao.response

import com.deepromeet.atcha.transit.domain.Fare

data class KakaoRouteResponse(
    val results: List<RouteResult>?
) {
    fun firstTaxiFare(): Fare? {
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
