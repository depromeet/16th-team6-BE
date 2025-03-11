package com.deepromeet.atcha.transit.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class SubwayStation(
    @Id
    val id: SubwayStationId,
    val stationCode: String,
    val name: String,
    val routeName: String,
    val routeCode: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SubwayStation) return false

        if (id != other.id) return false
        if (stationCode != other.stationCode) return false
        if (name != other.name) return false
        if (routeName != other.routeName) return false
        if (routeCode != other.routeCode) return false

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
