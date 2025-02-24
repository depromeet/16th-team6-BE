package com.deepromeet.atcha.location.domain

import org.springframework.stereotype.Service

@Service
class LocationService(
    private val locationReader: LocationReader
) {
    fun getLocations(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<Location> {
        return locationReader.read(keyword, currentCoordinate)
    }
}
