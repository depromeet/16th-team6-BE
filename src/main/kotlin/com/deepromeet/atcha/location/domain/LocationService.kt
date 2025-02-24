package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.dto.Cursor
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class LocationService(
    private val locationReader: LocationReader
) {
    fun getLocations(
        keyword: String,
        currentCoordinate: Coordinate,
        cursor: Cursor
    ): Slice<Location> {
        return locationReader.read(keyword, currentCoordinate, cursor)
    }
}
