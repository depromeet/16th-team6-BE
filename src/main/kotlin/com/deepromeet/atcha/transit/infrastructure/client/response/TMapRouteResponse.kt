package com.deepromeet.atcha.transit.infrastructure.client.response

data class TMapRouteResponse(
    val metaData: MetaData
)

data class MetaData(
    val plan: Plan
)

data class Plan(
    val itineraries: List<Itinerary>
)

data class Itinerary(
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
    val legs: List<Leg>
)

data class TMapFare(
    val regular: RegularFare
)

data class RegularFare(
    val currency: Currency,
    // 대중교통 요금
    val totalFare: Double
)

data class Currency(
    // 금액 상징 (￦)
    val symbol: String,
    // 금액 단위 (원)
    val currency: String,
    // 금액 단위 코드 (KRW)
    val currencyCode: String
)

data class Leg(
    // 구간별 이동 거리 (m)
    val distance: Int,
    // 구간별 소요 시간 (초)
    val sectionTime: Int,
    // 이동 수단 종류
    val mode: String,
    // 대중교통 노선 명칭
    val route: String?,
    // 노선 색상
    val routeColor: String?,
    // 노선 ID
    val routeId: String?,
    // 이동수단별 노선 코드
    val type: Int,
    // 광역 이동수단 요금
    val routePayment: Int?,
    // 이동수단 운행 여부 (1: 운행 중, 0: 운행 종료)
    val service: Int,
    // 다중 노선 정보 (있을 경우)
    val lane: List<Lane>?,
    // 출발 정보
    val start: Location,
    // 도착 정보
    val end: Location,
    // 도보 상세 정보 (도보 구간일 경우)
    val steps: List<Step>?,
    // 대중교통 구간 좌표
    val passShape: PassShape?,
    // 대중교통 구간 정류장 정보
    val passStopList: PassStopList?
)

data class Lane(
    // 이동수단 운행 여부 (1: 운행 중, 0: 운행 종료)
    val service: Int,
    // 다중 대중교통 노선 명칭
    val route: String,
    // 다중 대중교통 노선 색상
    val routeColor: String,
    // 다중 대중교통 노선 ID
    val routeId: String,
    // 이동수단별 노선 코드
    val type: Int
)

data class Location(
    // 위도
    val lat: Double,
    // 경도
    val lon: Double,
    // 정류장 명칭
    val name: String
)

data class Step(
    // 도보 이동 거리 (m)
    val distance: Double,
    // 도로명
    val streetName: String?,
    // 도보 구간 정보
    val description: String?,
    // 도보 구간 좌표
    val linestring: String?
)

data class PassShape(
    // 대중교통 구간 좌표
    val linestring: String
)

data class PassStopList(
    val stations: List<Station> = emptyList()
)

data class Station(
    // 순번
    val index: Int,
    // 정류장 ID
    val stationID: String,
    // 정류장 명칭
    val stationName: String,
    // 경도
    val lon: String,
    // 위도
    val lat: String
)
