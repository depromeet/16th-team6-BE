package com.deepromeet.atcha.location.api.request

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI

data class POIHistoryRequest(
    val name: String,
    val lat: Double,
    val lon: Double,
    val businessCategory: String,
    val address: String
) {
    init {
        require(lat in -90.0..90.0) { "잘못된 위도 값입니다: $lat (유효 범위: -90.0 ~ 90.0)" }
        require(lon in -180.0..180.0) { "잘못된 경도 값입니다: $lon (유효 범위: -180.0 ~ 180.0)" }
    }

    fun toPOI(): POI {
        return POI(
            location =
                Location(
                    name = name,
                    coordinate =
                        Coordinate(
                            lat = lat,
                            lon = lon
                        )
                ),
            businessCategory = businessCategory,
            address = address
        )
    }
}
