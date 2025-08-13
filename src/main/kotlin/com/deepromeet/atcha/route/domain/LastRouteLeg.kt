package com.deepromeet.atcha.route.domain

import com.deepromeet.atcha.transit.domain.RoutePassStops
import com.deepromeet.atcha.transit.domain.TimeDirection
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import java.time.LocalDateTime

data class LastRouteLeg(
    val distance: Int,
    val sectionTime: Int,
    val mode: RouteMode,
    val departureDateTime: LocalDateTime? = null,
    val route: String? = null,
    val type: String? = null,
    val service: String? = null,
    val start: RouteLocation,
    val end: RouteLocation,
    val steps: List<RouteStep>?,
    val passStops: RoutePassStops?,
    val pathCoordinates: String?,
    val transitInfo: TransitInfo
) {
    init {
        validateRouteMode()
        validateTransitDepartureTime()
    }

    val busInfo: TransitInfo.BusInfo?
        get() = transitInfo as? TransitInfo.BusInfo?

    val subwayInfo: TransitInfo.SubwayInfo?
        get() = transitInfo as? TransitInfo.SubwayInfo?

    fun requireBusInfo(): TransitInfo.BusInfo {
        return busInfo ?: throw IllegalStateException("버스 경로는 버스 정보가 필수입니다.")
    }

    fun requireSubwayInfo(): TransitInfo.SubwayInfo {
        return subwayInfo ?: throw IllegalStateException("지하철 경로는 지하철 정보가 필수입니다.")
    }

    fun isTransit(): Boolean = mode.isTransit()

    fun isWalk(): Boolean = mode.isWalk()

    fun isBus(): Boolean = mode == RouteMode.BUS

    fun resolveRouteName(): String {
        return route!!.split(":")[1]
    }

    fun toBusStationMeta(): BusStationMeta {
        return BusStationMeta(
            start.name,
            start.coordinate
        )
    }

    fun calcBoardingTime(
        targetTime: LocalDateTime,
        direction: TimeDirection
    ): LocalDateTime {
        return when (transitInfo) {
            is TransitInfo.SubwayInfo -> {
                transitInfo.timeTable.findNearestTime(targetTime, direction)
                    ?.departureTime
                    ?: throw TransitException.Companion.of(
                        TransitError.NOT_FOUND_SPECIFIED_TIME,
                        "지하철 '$route'의 ${start.name}역에서 " +
                            "$targetTime ${direction}의 시간표를 찾을 수 없습니다."
                    )
            }
            is TransitInfo.BusInfo -> {
                try {
                    transitInfo.timeTable.calculateNearestTime(targetTime, direction)
                } catch (e: TransitException) {
                    throw TransitException.Companion.of(
                        TransitError.NOT_FOUND_SPECIFIED_TIME,
                        "버스 '$route'의 ${start.name}정류장에서 " +
                            "$targetTime ${direction}의 시간표를 찾을 수 없습니다.",
                        e
                    )
                }
            }
            TransitInfo.NoInfoTable -> {
                throw TransitException.Companion.of(
                    TransitError.NOT_FOUND_SPECIFIED_TIME,
                    "해당 교통수단의 막차 시간 정보가 없습니다. " +
                        "$mode - ${start.name} -> ${end.name}"
                )
            }
        }
    }

    private fun validateRouteMode() {
        require(mode.isSupported) {
            "지원하지 않는 교통수단입니다: $mode"
        }
    }

    private fun validateTransitDepartureTime() {
        if (mode.requiresDepartureTime) {
            require(departureDateTime != null) {
                "대중교통($mode)의 출발시간은 필수입니다"
            }
        }
    }
}
