package com.deepromeet.atcha.transit.domain

interface ServiceRegionCandidatePolicy {
    fun candidates(station: BusStationMeta): List<ServiceRegion>
}
