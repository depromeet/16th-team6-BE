package com.deepromeet.atcha.transit.domain

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
        val startBranch = stationMap[startStationName] ?: return false
        val endBranch = stationMap[endStationName] ?: return false
        val finalBranch = stationMap[finalStationName] ?: return false

        val minOrd = minOf(startBranch.ord, finalBranch.ord)
        val maxOrd = maxOf(startBranch.ord, finalBranch.ord)
        return endBranch.ord in minOrd..maxOrd
    }

    fun isContains(
        startStationName: String,
        endStationName: String
    ): Boolean = startStationName in stationMap && endStationName in stationMap

    fun getDirection(
        startStationName: String,
        endStationName: String
    ): SubwayDirection {
        val startOrd = stationMap[startStationName]!!.ord
        val endOrd = stationMap[endStationName]!!.ord
        return if (startOrd < endOrd) SubwayDirection.DOWN else SubwayDirection.UP
    }
}
