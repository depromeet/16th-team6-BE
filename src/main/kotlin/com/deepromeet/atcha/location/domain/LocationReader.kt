package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.feign.FeignException
import com.deepromeet.atcha.location.exception.LocationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class LocationReader(
    private val poiFinder: POIFinder,
    private val reverseLabeler: ReverseLabeler
) {
    fun readPOIs(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        try {
            return poiFinder.find(keyword, currentCoordinate)
        } catch (e: FeignException) {
            logger.error(e) { e.message }
            throw LocationException.FailedToReadPOIs
        }
    }

    fun read(coordinate: Coordinate): Location {
        try {
            return reverseLabeler.label(coordinate)
        } catch (e: FeignException) {
            logger.error(e) { e.message }
            throw LocationException.FailedToReadLocation
        }
    }
}
