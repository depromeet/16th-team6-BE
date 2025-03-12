package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate

data class LastRoutesRequest(
    val startLat: String,
    val startLon: String,
    val endLat: String,
    val endLon: String
) {
    init {
        require(startLat.toDouble() in -90.0..90.0) { "잘못된 위도 값입니다: $startLat (유효 범위: -90.0 ~ 90.0)" }
        require(startLon.toDouble() in -180.0..180.0) { "잘못된 경도 값입니다: $startLon (유효 범위: -180.0 ~ 180.0)" }
        require(endLat.toDouble() in -90.0..90.0) { "잘못된 위도 값입니다: $endLat (유효 범위: -90.0 ~ 90.0)" }
        require(endLon.toDouble() in -180.0..180.0) { "잘못된 경도 값입니다: $endLon (유효 범위: -180.0 ~ 180.0)" }
    }

    fun toStart(): Coordinate = Coordinate(startLat.toDouble(), startLon.toDouble())

    fun toEnd(): Coordinate = Coordinate(endLat.toDouble(), endLon.toDouble())
}
