package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.user.application.UserReader
import com.deepromeet.atcha.user.domain.UserId
import org.springframework.stereotype.Service

@Service
class LocationService(
    private val locationReader: LocationReader,
    private val userReader: UserReader,
    private val poiHistoryManager: POIHistoryManager,
    private val serviceRegionValidator: ServiceRegionValidator
) {
    suspend fun getPOIs(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        return locationReader.readPOIs(keyword, currentCoordinate)
    }

    suspend fun getLocation(coordinate: Coordinate): Location {
        return locationReader.read(coordinate)
    }

    fun addPOIHistory(
        userId: UserId,
        poi: POI
    ) {
        val user = userReader.read(userId)
        poiHistoryManager.append(user, poi)
    }

    fun getPOIHistories(
        userId: UserId,
        currentCoordinate: Coordinate
    ): List<POI> {
        val user = userReader.read(userId)
        val pois = poiHistoryManager.getAll(user)
        return pois.map { it.distanceTo(currentCoordinate) }
    }

    fun removePOIHistory(
        userId: UserId,
        poi: POI
    ) {
        val user = userReader.read(userId)
        poiHistoryManager.remove(user, poi)
    }

    fun clearPOIHistories(userId: UserId) {
        val user = userReader.read(userId)
        poiHistoryManager.clear(user)
    }

    suspend fun isServiceRegion(coordinate: Coordinate): Boolean {
        try {
            serviceRegionValidator.validate(coordinate)
        } catch (e: TransitException) {
            return false
        }

        return true
    }
}
