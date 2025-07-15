package com.deepromeet.atcha.transit.domain.region

import com.deepromeet.atcha.transit.domain.bus.BusStationMeta

interface ServiceRegionCandidatePolicy {
    fun candidates(station: BusStationMeta): List<ServiceRegion>
}
