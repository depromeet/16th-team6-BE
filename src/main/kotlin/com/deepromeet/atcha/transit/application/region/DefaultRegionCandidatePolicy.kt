package com.deepromeet.atcha.transit.application.region

import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.domain.region.ServiceRegion
import org.springframework.stereotype.Component

@Component
class DefaultRegionCandidatePolicy(
    private val regionIdentifier: RegionIdentifier
) : ServiceRegionCandidatePolicy {
    override fun candidates(station: BusStationMeta): List<ServiceRegion> {
        val primary = regionIdentifier.identify(station.coordinate)

        val fallback =
            when (primary) {
                ServiceRegion.SEOUL -> listOf(ServiceRegion.GYEONGGI)
                ServiceRegion.GYEONGGI -> listOf(ServiceRegion.SEOUL)
                ServiceRegion.INCHEON -> listOf(ServiceRegion.GYEONGGI)
            }

        return listOf(primary) + fallback
    }
}
