package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.location.exception.LocationError
import com.deepromeet.atcha.location.exception.LocationException
import com.deepromeet.atcha.shared.exception.ExternalApiException
import org.springframework.stereotype.Component

@Component
class LocationReader(
    private val poiFinder: POIFinder,
    private val reverseGeocoder: ReverseGeocoder
) {
    suspend fun readPOIs(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        if (keyword.isBlank()) {
            return emptyList()
        }
        return poiFinder.find(keyword, currentCoordinate)
    }

    suspend fun read(coordinate: Coordinate): Location {
        try {
            return reverseGeocoder.geocode(coordinate)
        } catch (e: ExternalApiException) {
            throw LocationException.of(
                LocationError.FAILED_TO_READ_LOCATION,
                "좌표 (${coordinate.lat}, ${coordinate.lon})의 위치 정보를 읽어오는 중 오류가 발생했습니다.",
                e
            )
        }
    }
}
