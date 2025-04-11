package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate

data class BusStationMeta(
    val name: String,
    val coordinate: Coordinate
) {
    fun resolveName(): String {
        return name
            .replace("(지하)", "")
            .replace("(중)", "")
    }
}
