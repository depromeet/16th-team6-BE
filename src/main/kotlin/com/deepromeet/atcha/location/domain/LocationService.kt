package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service

const val USER_ID = 1L; // TODO: 향후 JWT 토큰에서 추출

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

    fun addPOIHistory(poi: POI) {
        val user = userReader.read(USER_ID)
        poiHistoryManager.append(user, poi)
    }

    fun getPOIHistories(): List<POI> {
        val user = userReader.read(USER_ID)
        return poiHistoryManager.getAll(user)
    }

    fun removePOIHistory(poi: POI) {
        val user = userReader.read(USER_ID)
        poiHistoryManager.remove(user, poi)
    }

    fun clearPOIHistories() {
        val user = userReader.read(USER_ID)
        poiHistoryManager.clear(user)
    }
}
