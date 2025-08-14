package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.ServiceRegion

interface RegionIdentifier {
    fun identify(coordinate: Coordinate): ServiceRegion
}
