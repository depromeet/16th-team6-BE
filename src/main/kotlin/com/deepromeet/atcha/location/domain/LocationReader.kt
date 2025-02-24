package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.feign.FeignException
import com.deepromeet.atcha.location.exception.LocationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class LocationReader(
    private val poiFinder: POIFinder
) {
    fun readPOIs(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        try {
            return poiFinder.find(keyword, currentCoordinate)
        } catch (e: FeignException) {
            logger.error(e) { "Failed to read POIs from TMap" }
            throw LocationException.FailedToReadPOIs
        }
    }

    fun read(coordinate: Coordinate): Location {
        try {
            tMapLocationClient.getReverseGeoLabel(
                coordinate.lat,
                coordinate.lon
            ).let {
                return it.toLocation()
            }
        } catch (e: FeignException) {
            logger.error(e) { "Failed to read location from TMap API" }
            throw LocationException.LocationApiError
        }
    }
}
