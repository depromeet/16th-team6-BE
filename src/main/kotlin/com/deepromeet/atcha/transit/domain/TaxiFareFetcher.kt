package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate

interface TaxiFareFetcher {
    fun fetch(
        originCoordinate: Coordinate,
        destinationCoordinate: Coordinate
    ): Fare?
}
