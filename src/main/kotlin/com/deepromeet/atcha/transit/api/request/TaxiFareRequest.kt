package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.exception.LocationError
import com.deepromeet.atcha.location.exception.LocationException

data class TaxiFareRequest(
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double
) {
    init {
        require(startLat in -90.0..90.0) {
            throw LocationException.of(
                LocationError.INVALID_LATITUDE,
                "시작 위도는 -90.0에서 90.0 사이에 있어야 합니다. 입력된 값: $startLat"
            )
        }
        require(startLon in -180.0..180.0) {
            throw LocationException.of(
                LocationError.INVALID_LONGITUDE,
                "시작 경도는 -180.0에서 180.0 사이에 있어야 합니다. 입력된 값: $startLon"
            )
        }
        require(endLat in -90.0..90.0) {
            throw LocationException.of(
                LocationError.INVALID_LATITUDE,
                "도착 위도는 -90.0에서 90.0 사이에 있어야 합니다. 입력된 값: $endLat"
            )
        }
        require(endLon in -180.0..180.0) {
            throw LocationException.of(
                LocationError.INVALID_LONGITUDE,
                "도착 경도는 -180.0에서 180.0 사이에 있어야 합니다. 입력된 값: $endLon"
            )
        }
    }

    fun toStart(): Coordinate = Coordinate(startLat, startLon)

    fun toEnd(): Coordinate = Coordinate(endLat, endLon)
}
