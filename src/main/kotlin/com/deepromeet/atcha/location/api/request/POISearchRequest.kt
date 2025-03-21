package com.deepromeet.atcha.location.api.request

import com.deepromeet.atcha.location.domain.Coordinate

data class POISearchRequest(
    val keyword: String,
    val lat: Double,
    val lon: Double
) {
    init {
        require(lat in -90.0..90.0) { "잘못된 위도 값입니다: $lat (유효 범위: -90.0 ~ 90.0)" }
        require(lon in -180.0..180.0) { "잘못된 경도 값입니다: $lon (유효 범위: -180.0 ~ 180.0)" }
    }

    fun toCoordinate() = Coordinate(lat, lon)
}
