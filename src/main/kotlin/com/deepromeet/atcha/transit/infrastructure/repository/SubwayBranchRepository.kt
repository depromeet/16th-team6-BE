package com.deepromeet.atcha.transit.infrastructure.repository

import com.deepromeet.atcha.transit.domain.SubwayBranch
import org.springframework.data.jpa.repository.JpaRepository

interface SubwayBranchRepository : JpaRepository<SubwayBranch, Long> {
    fun findByRouteCode(routeCode: String): MutableList<SubwayBranch>
}
