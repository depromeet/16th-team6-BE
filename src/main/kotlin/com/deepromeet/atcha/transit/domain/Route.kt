package com.deepromeet.atcha.transit.domain

data class Plan(
    val itineraries: List<Itinerary>
)

data class Itinerary(
    val totalTime: Int, // 총 소요시간 (초)
    val transferCount: Int, // 환승 횟수
    val totalWalkDistance: Int, // 총 보행 거리 (m)
    val totalDistance: Int, // 총 이동 거리 (m)
    val totalWalkTime: Int, // 총 보행 소요 시간 (초)
    val fare: Fare,
    val legs: List<Leg>
)

data class Fare(
    val regular: RegularFare
)

data class RegularFare(
    val currency: Currency,
    val totalFare: Double // 대중교통 요금
)

data class Currency(
    val symbol: String, // 금액 상징 (￦)
    val currency: String, // 금액 단위 (원)
    val currencyCode: String // 금액 단위 코드 (KRW)
)

data class Leg(
    val distance: Int, // 구간별 이동 거리 (m)
    val sectionTime: Int, // 구간별 소요 시간 (초)
    val mode: String, // 이동 수단 종류
    val route: String?, // 대중교통 노선 명칭
    val routeColor: String?, // 노선 색상
    val routeId: String?, // 노선 ID
    val type: Int, // 이동수단별 노선 코드
    val routePayment: Int?, // 광역 이동수단 요금
    val service: Int, // 이동수단 운행 여부 (1: 운행 중, 0: 운행 종료)
    val lane: List<Lane>?, // 다중 노선 정보 (있을 경우)
    val start: Location, // 출발 정보
    val end: Location, // 도착 정보
    val steps: List<Step>?, // 도보 상세 정보 (도보 구간일 경우)
    val passShape: PassShape?, // 대중교통 구간 좌표
    val passStopList: PassStopList? // 대중교통 구간 정류장 정보
)

data class Lane(
    val service: Int, // 이동수단 운행 여부 (1: 운행 중, 0: 운행 종료)
    val route: String, // 다중 대중교통 노선 명칭
    val routeColor: String, // 다중 대중교통 노선 색상
    val routeId: String, // 다중 대중교통 노선 ID
    val type: Int // 이동수단별 노선 코드
)

data class Location(
    val lat: Double, // 위도
    val lon: Double, // 경도
    val name: String // 정류장 명칭
)

data class Step(
    val distance: Double, // 도보 이동 거리 (m)
    val streetName: String?, // 도로명
    val description: String?, // 도보 구간 정보
    val linestring: String? // 도보 구간 좌표
)

data class PassShape(
    val linestring: String // 대중교통 구간 좌표
)

data class PassStopList(
    val stations: List<Station> = emptyList()
)

data class Station(
    val index: Int, // 순번
    val stationID: String, // 정류장 ID
    val stationName: String, // 정류장 명칭
    val lon: String, // 경도
    val lat: String // 위도
)
