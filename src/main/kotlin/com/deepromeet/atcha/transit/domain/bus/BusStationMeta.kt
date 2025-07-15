package com.deepromeet.atcha.transit.domain.bus

import com.deepromeet.atcha.location.domain.Coordinate

data class BusStationMeta(
    val name: String,
    val coordinate: Coordinate
) {
    fun resolveName(): String = normalize(name)

    private fun normalize(name: String): String =
        name.replace("(지하)", "")
            .replace("(중)", "")
            .trim()
}
