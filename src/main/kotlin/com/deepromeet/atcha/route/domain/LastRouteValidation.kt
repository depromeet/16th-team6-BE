package com.deepromeet.atcha.route.domain

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDateTime
import java.time.LocalTime

fun LastRoute.isValidLastRoute(): Boolean {
    val log = KotlinLogging.logger {}
    val time = this.departureDateTime.toLocalTime()
    val isValid = (isAfter9PM(time) || isBefore4AM(time)) && isAfterNow(this.departureDateTime)

    if (!isValid) {
        log.debug { "막차 시간대가 아닙니다. 입력값: ${this.departureDateTime} (유효 시간: 21:00-03:59)" }
    }

    return isValid
}

private fun isAfterNow(time: LocalDateTime): Boolean = time.isAfter(LocalDateTime.now())

private fun isBefore4AM(time: LocalTime): Boolean = time.isBefore(LocalTime.of(4, 0))

private fun isAfter9PM(time: LocalTime): Boolean = time.isAfter(LocalTime.of(21, 0))
