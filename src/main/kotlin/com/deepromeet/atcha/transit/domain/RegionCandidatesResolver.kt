package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import org.springframework.stereotype.Component

@Component
class RegionCandidatesResolver(
    private val regionIdentifier: RegionIdentifier
) {
    fun resolve(
        origin: Coordinate,
        destination: Coordinate
    ): List<ServiceRegion> {
        val originRegion = regionIdentifier.identify(origin)
        val list = mutableListOf(originRegion)

        destination.let {
            val destRegion = regionIdentifier.identify(it)
            if (destRegion != originRegion) list += destRegion
        }

        return list
    }
}
