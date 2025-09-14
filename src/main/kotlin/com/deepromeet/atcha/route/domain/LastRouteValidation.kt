package com.deepromeet.atcha.route.domain

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalTime

fun LastRoute.isValidDepartureTime(): Boolean {
    val log = KotlinLogging.logger {}
    val time = this.departureDateTime.toLocalTime()
    val isValid = time.isAfter(LocalTime.of(21, 0)) || time.isBefore(LocalTime.of(4, 0))

    if (!isValid) {
        log.debug { "막차 시간대가 아닙니다. 입력값: ${this.departureDateTime} (유효 시간: 21:00-03:59)" }
    }

    return isValid
}
