package com.deepromeet.atcha.location.domain

interface POIFinder {
    fun find(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI>
}
