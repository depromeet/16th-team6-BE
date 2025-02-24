package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.feign.FeignException
import com.deepromeet.atcha.location.exception.LocationException
import com.deepromeet.atcha.location.infrastructure.client.TMapLocationClient
import org.springframework.stereotype.Component

@Component
class LocationReader(
    private val tMapLocationClient: TMapLocationClient
) {
    fun read(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<Location> {
        try {
            tMapLocationClient.getPOIs(
                keyword,
                currentCoordinate.lat,
                currentCoordinate.lon
            ).let {
                return it.toLocations()
            }
        } catch (e: FeignException) {
            throw LocationException.LocationApiError
        }
    }
}
