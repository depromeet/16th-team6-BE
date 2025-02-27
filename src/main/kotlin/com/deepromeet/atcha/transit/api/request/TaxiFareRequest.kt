package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate

data class TaxiFareRequest(
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double
) {
    init {
        require(startLat in -90.0..90.0) { "잘못된 위도 값입니다: $startLat (유효 범위: -90.0 ~ 90.0)" }
        require(startLon in -180.0..180.0) { "잘못된 경도 값입니다: $startLon (유효 범위: -180.0 ~ 180.0)" }
        require(endLat in -90.0..90.0) { "잘못된 위도 값입니다: $endLat (유효 범위: -90.0 ~ 90.0)" }
        require(endLon in -180.0..180.0) { "잘못된 경도 값입니다: $endLon (유효 범위: -180.0 ~ 180.0)" }
    }

    fun toStart(): Coordinate = Coordinate(startLat, startLon)

    fun toEnd(): Coordinate = Coordinate(endLat, endLon)
}
