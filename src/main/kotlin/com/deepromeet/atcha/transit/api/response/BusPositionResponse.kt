package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.BusCongestion
import com.deepromeet.atcha.transit.domain.BusPosition
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BusPositionResponse(
    val vehicleId: String,
    val sectionOrder: Int,
    val vehicleNumber: String,
    val sectionProgress: Double,
    val busCongestion: BusCongestion,
    val remainSeats: Int? = null
) {
    constructor(
        busPosition: BusPosition
    ) : this(
        vehicleId = busPosition.vehicleId,
        sectionOrder = busPosition.sectionOrder,
        vehicleNumber = busPosition.vehicleNumber,
        sectionProgress = busPosition.calculateSectionProgress(),
        busCongestion = busPosition.busCongestion,
        remainSeats =
            if (busPosition.busCongestion == BusCongestion.LOW &&
                busPosition.remainSeats == 0
            ) {
                null
            } else {
                busPosition.remainSeats
            }
    )
}
