package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.ServiceRegion

interface RegionIdentifier {
    suspend fun identify(coordinate: Coordinate): ServiceRegion
}
