package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.POI

interface POIFinder {
    fun find(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI>
}
