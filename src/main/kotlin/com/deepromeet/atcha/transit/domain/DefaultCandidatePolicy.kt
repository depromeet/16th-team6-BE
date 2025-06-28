package com.deepromeet.atcha.transit.domain

import org.springframework.stereotype.Component

@Component
class DefaultCandidatePolicy(
    private val regionIdentifier: RegionIdentifier
) : ServiceRegionCandidatePolicy {
    override fun candidates(station: BusStationMeta): List<ServiceRegion> {
        val primary = regionIdentifier.identify(station.coordinate)

        val fallback =
            when (primary) {
                ServiceRegion.SEOUL -> ServiceRegion.GYEONGGI
                ServiceRegion.GYEONGGI -> ServiceRegion.SEOUL
                ServiceRegion.INCHEON -> ServiceRegion.GYEONGGI
            }

        return listOf(primary, fallback)
    }
}
