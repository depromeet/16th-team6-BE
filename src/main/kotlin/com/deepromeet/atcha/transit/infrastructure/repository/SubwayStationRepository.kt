package com.deepromeet.atcha.transit.infrastructure.repository

import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationId
import org.springframework.data.jpa.repository.JpaRepository

interface SubwayStationRepository : JpaRepository<SubwayStation, SubwayStationId> {
    fun findByRouteNameAndNameContaining(
        routeName: String,
        name: String
    ): SubwayStation?
}
