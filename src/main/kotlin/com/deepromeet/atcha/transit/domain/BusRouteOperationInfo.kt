package com.deepromeet.atcha.transit.domain

import java.time.LocalDateTime

data class BusRouteOperationInfo(
    val startStationName: String,
    val endStationName: String,
    val serviceHours: List<BusServiceHours>
) {
    fun getTimeTable(
        dailyType: DailyType,
        busDirection: BusDirection
    ): BusTimeTable? {
        if (serviceHours.size == 1) {
            return serviceHours.first().toBusTimeTable()
        }

        for (dt in priority(dailyType)) {
            val sameDay = serviceHours.filter { it.dailyType == dt }
            if (sameDay.isEmpty()) continue

            sameDay.firstOrNull { it.busDirection == busDirection }
                ?.let { return it.toBusTimeTable() }

            sameDay.firstOrNull { it.busDirection == BusDirection.UP }
                ?.let { return it.toBusTimeTable() }
        }

        return null
    }

    private fun priority(dt: DailyType) =
        when (dt) {
            DailyType.WEEKDAY -> listOf(DailyType.WEEKDAY)
            DailyType.SATURDAY -> listOf(DailyType.SATURDAY, DailyType.SUNDAY, DailyType.WEEKDAY)
            DailyType.SUNDAY -> listOf(DailyType.SUNDAY, DailyType.HOLIDAY, DailyType.WEEKDAY)
            DailyType.HOLIDAY -> listOf(DailyType.HOLIDAY, DailyType.SUNDAY, DailyType.WEEKDAY)
        }
}

data class BusServiceHours(
    val dailyType: DailyType,
    val busDirection: BusDirection = BusDirection.UP,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val term: Int
) {
    fun toBusTimeTable(): BusTimeTable {
        return BusTimeTable(
            startTime,
            endTime,
            term
        )
    }
}
