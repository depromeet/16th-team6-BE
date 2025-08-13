package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta

interface ServiceRegionCandidatePolicy {
    fun candidates(station: BusStationMeta): List<ServiceRegion>
}
