package com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response

import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusServiceHours
import com.deepromeet.atcha.transit.domain.BusTimeParser
import com.deepromeet.atcha.transit.domain.DailyType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SeoulBusOperationResponse(
    val rows: List<SeoulBusOperationRow>
) {
    fun toBusRouteOperationInfo(): BusRouteOperationInfo? {
        val row = rows.firstOrNull() ?: return null

        val serviceHoursList = mutableListOf<BusServiceHours>()

        val today = LocalDate.now()

        serviceHoursList +=
            BusServiceHours(
                dailyType = DailyType.WEEKDAY,
                startTime =
                    BusTimeParser.parseTime(
                        row.timeFirst, today,
                        SeoulBusOperationRow.Companion.TIME_FORMATTER
                    ),
                endTime =
                    BusTimeParser.parseTime(
                        row.timeLast, today,
                        SeoulBusOperationRow.Companion.TIME_FORMATTER
                    ),
                term = row.norTerms.toIntOrNull() ?: 0
            )

        serviceHoursList +=
            BusServiceHours(
                dailyType = DailyType.SATURDAY,
                startTime =
                    BusTimeParser.parseTime(
                        row.satTimeFirst, today,
                        SeoulBusOperationRow.Companion.TIME_FORMATTER
                    ),
                endTime =
                    BusTimeParser.parseTime(
                        row.satTimeLast, today,
                        SeoulBusOperationRow.Companion.TIME_FORMATTER
                    ),
                term = row.satTerms.toIntOrNull() ?: 0
            )

        serviceHoursList +=
            BusServiceHours(
                dailyType = DailyType.HOLIDAY,
                startTime =
                    BusTimeParser.parseTime(
                        row.holTimeFirst, today,
                        SeoulBusOperationRow.Companion.TIME_FORMATTER
                    ),
                endTime =
                    BusTimeParser.parseTime(
                        row.holTimeLast, today,
                        SeoulBusOperationRow.Companion.TIME_FORMATTER
                    ),
                term = row.holTerms.toIntOrNull() ?: 0
            )

        return BusRouteOperationInfo(
            startStationName = row.stnFirst,
            endStationName = row.stnLast,
            serviceHours = serviceHoursList
        )
    }
}

data class SeoulBusOperationRow(
    val routId: String,
    val routName: String,
    val rtCd: Int,
    val routType: String,
    val tmpOrder: Int,
    val stnFirst: String,
    val stnLast: String,
    val busIntervals: Int,
    val timeFirst: String,
    val timeLast: String,
    val satTimeFirst: String,
    val satTimeLast: String,
    val holTimeFirst: String,
    val holTimeLast: String,
    val routeDistance: Int,
    val busFee: Int,
    val busRunStatusCd: String,
    val busRunStatus: String?,
    val turnPlaceStationId: String,
    val normalDayFirstBusTime: String,
    val normalDayLastBusTime: String,
    val normalLastRouteAt: String,
    val normalLastBusId: String,
    val lowerDayFirstBusTime: String,
    val lowerDayLastBusTime: String,
    val lowerLastRouteAt: String,
    val lowerLastBusId: String,
    val timegap: Int,
    val norTerms: String,
    val satTerms: String,
    val holTerms: String,
    val routNo: String,
    val companyNm: String,
    val faxNo: String?,
    val telNo: String,
    val email: String?,
    val etcDesc: String?,
    val rn: Int,
    val rnum: Int
) {
    companion object {
        val TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm")
    }
}
