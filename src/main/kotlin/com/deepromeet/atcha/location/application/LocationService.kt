package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.user.application.UserReader
import org.springframework.stereotype.Service

@Service
class LocationService(
    private val locationReader: LocationReader,
    private val userReader: UserReader,
    private val poiHistoryManager: POIHistoryManager
) {
    fun getPOIs(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        return locationReader.readPOIs(keyword, currentCoordinate)
    }

    fun getLocation(coordinate: Coordinate): Location {
        return locationReader.read(coordinate)
    }

    fun addPOIHistory(
        userId: Long,
        poi: POI
    ) {
        val user = userReader.read(userId)
        poiHistoryManager.append(user, poi)
    }

    fun getPOIHistories(
        userId: Long,
        currentCoordinate: Coordinate
    ): List<POI> {
        val user = userReader.read(userId)
        val pois = poiHistoryManager.getAll(user)
        return pois.map { it.distanceTo(currentCoordinate) }
    }

    fun removePOIHistory(
        userId: Long,
        poi: POI
    ) {
        val user = userReader.read(userId)
        poiHistoryManager.remove(user, poi)
    }

    fun clearPOIHistories(userId: Long) {
        val user = userReader.read(userId)
        poiHistoryManager.clear(user)
    }
}
