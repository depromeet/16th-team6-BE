package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.transit.domain.bus.BusPosition

interface StartedBusCache {
    fun get(id: String): BusPosition?

    fun cache(
        id: String,
        pos: BusPosition
    )
}
