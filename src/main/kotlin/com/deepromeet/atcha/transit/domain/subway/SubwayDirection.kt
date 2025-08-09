package com.deepromeet.atcha.transit.domain.subway

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

enum class SubwayDirection(
    val code: String,
    val description: String
) {
    UP("U", "상행/내선"),
    DOWN("D", "하행/외선");

    fun getName(isCircular: Boolean): String {
        return when (this) {
            UP -> if (isCircular) "내선" else "상행"
            DOWN -> if (isCircular) "외선" else "하행"
        }
    }

    companion object {
        fun fromCode(code: String): SubwayDirection = entries.first { it.code == code }

        fun resolve(
            routes: List<Route>,
            startStation: SubwayStation,
            nextStation: SubwayStation,
            endStation: SubwayStation
        ): SubwayDirection {
            return routes.firstOrNull { it.isContains(startStation.name, nextStation.name, endStation.name) }
                ?.getDirection(startStation.name, nextStation.name)
                ?: run {
                    log.warn { "지하철 방향 추정 실패: 출발역='${startStation.name}', 도착역='${nextStation.name}'" }
                    throw TransitException.of(
                        TransitError.NOT_FOUND_SUBWAY_ROUTE,
                        "출발역 '${startStation.name}'에서 도착역 '${nextStation.name}'으로 가는 지하철 노선을 찾을 수 없습니다."
                    )
                }
        }

        fun fromName(name: String): SubwayDirection {
            return when (name) {
                "상행", "내선" -> {
                    UP
                }
                "하행", "외선" -> {
                    DOWN
                }
                else -> {
                    log.warn { "지하철 방향 이름 해석 실패: 이름='$name'" }
                    throw TransitException.of(
                        TransitError.INVALID_DIRECTION_NAME,
                        "지하철 방향 이름 '$name'이 유효하지 않습니다."
                    )
                }
            }
        }
    }
}
