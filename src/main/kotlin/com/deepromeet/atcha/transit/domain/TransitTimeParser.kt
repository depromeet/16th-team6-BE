package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.domain.subway.DayScope
import com.deepromeet.atcha.transit.domain.subway.SubwayTime
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TransitTimeParser {
    fun parseTime(
        timeStr: String?,
        baseDate: LocalDate,
        format: DateTimeFormatter
    ): LocalDateTime {
        if (timeStr.isNullOrBlank() || timeStr == "0") {
            throw TransitException.of(
                TransitError.INVALID_TIME_FORMAT,
                "시간 정보가 유효하지 않습니다: '$timeStr'"
            )
        }

        try {
            val localTime = LocalTime.parse(timeStr, format)

            // 새벽 3시 이전이면 다음 날로 변경
            val effectiveDate =
                if (localTime.isBefore(LocalTime.of(3, 0))) {
                    baseDate.plusDays(1)
                } else {
                    baseDate
                }

            return LocalDateTime.of(effectiveDate, localTime)
        } catch (e: Exception) {
            throw TransitException.of(
                TransitError.INVALID_TIME_FORMAT,
                "시간 형식 파싱에 실패했습니다: 입력='$timeStr', 형식='$format'",
                e
            )
        }
    }

    fun parseTime(timeStr: String?): SubwayTime? {
        return try {
            if (timeStr.isNullOrBlank()) {
                return null
            }

            val parts = timeStr.split(":")
            var hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val second = parts[2].toInt()

            val dayScope =
                if (hour >= 24) {
                    hour -= 24
                    DayScope.TOMORROW
                } else {
                    DayScope.TODAY
                }

            val localTime = LocalTime.of(hour, minute, second)
            SubwayTime(localTime, dayScope)
        } catch (e: Exception) {
            return null
        }
    }
}
