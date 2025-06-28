package com.deepromeet.atcha.transit.domain

interface StartedBusCache {
    fun get(id: String): BusPosition?

    fun cache(
        id: String,
        pos: BusPosition
    )
}
