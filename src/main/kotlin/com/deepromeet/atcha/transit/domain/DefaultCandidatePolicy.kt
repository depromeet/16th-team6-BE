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
                ServiceRegion.SEOUL -> listOf(ServiceRegion.GYEONGGI)
                ServiceRegion.GYEONGGI -> listOf(ServiceRegion.SEOUL, ServiceRegion.INCHEON)
                ServiceRegion.INCHEON -> listOf(ServiceRegion.GYEONGGI)
            }

        return listOf(primary) + fallback
    }
}
