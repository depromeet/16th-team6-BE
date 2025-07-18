package com.deepromeet.atcha.transit.application.region

import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.domain.region.ServiceRegion

interface ServiceRegionCandidatePolicy {
    fun candidates(station: BusStationMeta): List<ServiceRegion>
}
