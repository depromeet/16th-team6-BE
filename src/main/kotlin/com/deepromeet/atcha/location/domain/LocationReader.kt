package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.dto.Cursor
import com.deepromeet.atcha.location.infrastructure.client.TMapLocationClient
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component

@Component
class LocationReader(
    private val tMapLocationClient: TMapLocationClient
) {
    fun read(
        keyword: String,
        currentCoordinate: Coordinate,
        cursor: Cursor
    ): Slice<Location> {
        tMapLocationClient.getPOIs(
            keyword,
            currentCoordinate.lat,
            currentCoordinate.lon,
            cursor.page,
            cursor.size
        ).let {
            return it.toLocations()
        }
    }
}
