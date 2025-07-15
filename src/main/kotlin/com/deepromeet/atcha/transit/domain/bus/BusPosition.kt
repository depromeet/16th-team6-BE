package com.deepromeet.atcha.transit.domain.bus

data class BusPosition(
    val vehicleId: String,
    val sectionOrder: Int,
    val vehicleNumber: String,
    val fullSectionDistance: Double?,
    val currentSectionDistance: Double?,
    val busCongestion: BusCongestion,
    val remainSeats: Int? = null
) {
    fun calculateSectionProgress(): Double {
        if (fullSectionDistance == null || currentSectionDistance == null || fullSectionDistance == 0.0) {
            return 0.0
        }

        return currentSectionDistance / fullSectionDistance
    }
}
