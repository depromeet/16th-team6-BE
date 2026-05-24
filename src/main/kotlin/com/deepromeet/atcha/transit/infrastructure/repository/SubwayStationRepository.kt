package com.deepromeet.atcha.transit.infrastructure.repository

import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.deepromeet.atcha.transit.domain.subway.SubwayStationId
import org.springframework.data.jpa.repository.JpaRepository

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

    fun findFirstByRouteCodeAndNormalizedName(
        routeCode: String,
        normalizedName: String
    ): SubwayStation?

    fun findFirstByRouteCodeAndNameLike(
        routeCode: String,
        namePattern: String
    ): SubwayStation?
}
