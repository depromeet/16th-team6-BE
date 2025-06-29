package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object BusTimeParser {
    fun parseDateTime(
        timeStr: String?,
        format: DateTimeFormatter
    ): LocalDateTime {
        // 입력 값 유효성 검사
        if (timeStr.isNullOrBlank() || timeStr == "0") {
            throw TransitException.of(
                TransitError.INVALID_TIME_FORMAT,
                "시간 정보가 유효하지 않습니다(null, blank, or '0'): '$timeStr'"
            )
        }

        try {
            return LocalDateTime.parse(timeStr, format)
        } catch (e: Exception) {
            throw TransitException.of(
                TransitError.INVALID_TIME_FORMAT,
                "시간 형식 파싱에 실패했습니다: 입력='$timeStr', 형식='$format'",
                e
            )
        }
    }

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
}
