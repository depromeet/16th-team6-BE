package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.dto.Cursor
import com.deepromeet.atcha.location.infrastructure.client.TMapLocationClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component

@Component
class LocationReader(
    private val tMapLocationClient: TMapLocationClient,
    @Value("\${tmap.api.app-key}")
    private val appKey: String
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
            cursor.size,
            appKey
        ).let {
            return it.toLocations()
        }
    }

    fun read(coordinate: Coordinate) {
        TODO()
    }
}
