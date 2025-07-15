package com.deepromeet.atcha.transit.domain.bus

interface StartedBusCache {
    fun get(id: String): BusPosition?

    fun cache(
        id: String,
        pos: BusPosition
    )
}
