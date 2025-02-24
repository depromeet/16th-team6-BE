package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.feign.FeignException
import com.deepromeet.atcha.location.exception.LocationException
import com.deepromeet.atcha.location.infrastructure.client.TMapLocationClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

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
            logger.error(e) { "Failed to read locations from TMap API" }
            throw LocationException.LocationApiError
        }
    }
}
