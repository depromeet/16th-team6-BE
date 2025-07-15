package com.deepromeet.atcha.transit.domain.region

import com.deepromeet.atcha.location.domain.Coordinate
import org.springframework.stereotype.Component

@Component
class ServiceRegionValidator(
    private val regionIdentifier: RegionIdentifier
) {
    fun validate(vararg coordinates: Coordinate) {
        coordinates.forEach { coordinate ->
            regionIdentifier.identify(coordinate)
        }
    }
}
