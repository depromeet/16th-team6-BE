package com.deepromeet.atcha.transit.infrastructure.client.odsay.response

import com.deepromeet.atcha.transit.infrastructure.client.kakao.response.KakaoFare
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Leg

data class ODSayRouteResponse(
    val metaData: MetaData?,
    val result: Result?
)

data class Result(
    val status: Int,
    val message: String
)

data class MetaData(
    val plan: Plan
)

data class Plan(
    val itineraries: List<ODSayItinerary>
)

data class ODSayItinerary(
    // 총 소요시간 (초)
    val totalTime: Int,
    // 환승 횟수
    val transferCount: Int,
    // 총 보행 거리 (m)
    val totalWalkDistance: Int,
    // 총 이동 거리 (m)
    val totalDistance: Int,
    // 총 보행 소요 시간 (초)
    val totalWalkTime: Int,
    val fare: KakaoFare,
    val legs: List<Leg>,
    val pathType: Int
)
