package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate

interface RegionIdentifier {
    fun identify(coordinate: Coordinate): BusRegion
}
