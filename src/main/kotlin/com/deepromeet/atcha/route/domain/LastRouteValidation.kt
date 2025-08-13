package com.deepromeet.atcha.route.domain

import java.time.LocalDateTime
import java.time.LocalTime

fun LocalDateTime.validateLastRouteDeparture(): LocalDateTime {
    val time = this.toLocalTime()
    require(time.isAfter(LocalTime.of(20, 0)) || time.isBefore(LocalTime.of(3, 0))) {
        "유효하지않은 막차 시간 범위입니다. 입력값: $this"
    }
    return this
}
