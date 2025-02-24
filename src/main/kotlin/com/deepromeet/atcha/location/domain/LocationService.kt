package com.deepromeet.atcha.location.domain

import org.springframework.stereotype.Service

@Service
class LocationService(
    private val locationReader: LocationReader
) {
    fun getPOIs(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        return locationReader.readPOIs(keyword, currentCoordinate)
    }
}
