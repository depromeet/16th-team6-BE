package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException

enum class SubwayDirection(
    val code: String,
    val description: String
) {
    UP("U", "상행/내선"),
    DOWN("D", "하행/외선");

    companion object {
        fun fromCode(code: String): SubwayDirection = entries.first { it.code == code }

        fun resolve(
            routes: List<Route>,
            startStation: SubwayStation,
            endStation: SubwayStation
        ): SubwayDirection {
            return routes.firstOrNull { it.isContains(startStation.name, endStation.name) }
                ?.getDirection(startStation.name, endStation.name)
                ?: run {
                    log.warn { "지하철 방향 추정 실패: 출발역='${startStation.name}', 도착역='${endStation.name}'" }
                    throw TransitException.NotFoundSubwayRoute
                }
        }
    }
}
