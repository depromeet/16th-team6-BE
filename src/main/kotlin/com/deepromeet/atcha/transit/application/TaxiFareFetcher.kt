package com.deepromeet.atcha.transit.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.Fare

interface TaxiFareFetcher {
    fun fetch(
        start: Coordinate,
        end: Coordinate
    ): Fare
}
