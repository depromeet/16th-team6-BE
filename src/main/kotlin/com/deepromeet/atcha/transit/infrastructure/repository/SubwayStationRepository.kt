package com.deepromeet.atcha.transit.infrastructure.repository

import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SubwayStationRepository : JpaRepository<SubwayStation, SubwayStationId> {
    fun findByRouteCodeAndName(
        routeCode: String,
        name: String
    ): SubwayStation?

    @Query("SELECT MAX(ord) FROM SubwayStation WHERE routeName = :routeName")
    fun getStationMaxOrd(routeName: String): Int

    fun findByRouteCode(routeCode: String): List<SubwayStation>
}
