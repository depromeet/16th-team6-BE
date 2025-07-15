package com.deepromeet.atcha.transit.domain.region

import com.deepromeet.atcha.location.domain.Coordinate

interface RegionIdentifier {
    fun identify(coordinate: Coordinate): ServiceRegion
}
