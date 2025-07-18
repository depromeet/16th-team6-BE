package com.deepromeet.atcha.transit.application.region

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.region.ServiceRegion

interface RegionIdentifier {
    fun identify(coordinate: Coordinate): ServiceRegion
}
