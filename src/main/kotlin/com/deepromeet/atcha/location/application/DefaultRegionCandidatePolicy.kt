package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
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
                ServiceRegion.GYEONGGI -> listOf(ServiceRegion.SEOUL, ServiceRegion.INCHEON)
                ServiceRegion.INCHEON -> listOf(ServiceRegion.GYEONGGI)
            }

        return listOf(primary) + fallback
    }
}
