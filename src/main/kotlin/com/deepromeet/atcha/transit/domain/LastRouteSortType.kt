package com.deepromeet.atcha.transit.domain

enum class LastRouteSortType(val code: Int) {
    MINIMUM_TRANSFERS(1),
    DEPARTURE_TIME_DESC(2)
    ;

    companion object {
        fun fromCode(code: Int): LastRouteSortType? = entries.find { it.code == code }
    }
}
