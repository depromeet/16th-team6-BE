package com.deepromeet.atcha.transit.domain.subway

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class SubwayBranch(
    @Id
    val id: Long,
    val routeName: String,
    val routeCode: String,
    val ord: Int,
    val stationName: String,
    val finalStationName: String
)

data class Route(val list: List<SubwayBranch>) {
    private val stationMap by lazy { list.associateBy { it.stationName } }

    fun isReachable(
        startStationName: String,
        endStationName: String,
        finalStationName: String
    ): Boolean {
        val start = stationMap[startStationName] ?: return false
        val end = stationMap[endStationName] ?: return false
        val final = stationMap[finalStationName] ?: return false

        val minOrd = minOf(start.ord, final.ord)
        val maxOrd = maxOf(start.ord, final.ord)
        return end.ord in minOrd..maxOrd
    }

    fun isContains(
        startStationName: String,
        nextStationName: String,
        endStationName: String,
    ): Boolean = startStationName in stationMap && nextStationName in stationMap && endStationName in stationMap

    fun getDirection(
        startStationName: String,
        nextStation: String
    ): SubwayDirection {
        val start = stationMap[startStationName]!!
        val end = stationMap[nextStation]!!

        val isLine9 = start.routeCode == "9"
        val isDown = if (isLine9) start.ord > end.ord else start.ord < end.ord

        return if (isDown) SubwayDirection.DOWN else SubwayDirection.UP
    }
}
