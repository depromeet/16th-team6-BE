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

    fun findByRouteCodeAndNameContains(
        routeCode: String,
        name: String
    ): SubwayStation?

    fun findByRouteCode(routeCode: String): List<SubwayStation>

    @Query(
        """
        SELECT s
        FROM SubwayStation s
        WHERE s.routeCode = :routeCode
        AND ( s.name = :name OR s.name LIKE CONCAT(:name, '(%') )
        """
    )
    fun findStationByNameAndRoute(
        routeCode: String,
        name: String
    ): SubwayStation?
}
