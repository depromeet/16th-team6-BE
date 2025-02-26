package com.deepromeet.atcha.transit.infrastructure.client

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.Fare
import com.deepromeet.atcha.transit.domain.TaxiFareFetcher
import org.springframework.stereotype.Component

@Component
class KakaoTaxiFareClient(
    private val kakaoRouteFeignClient: KakaoRouteFeignClient
) : TaxiFareFetcher {
    override fun fetch(
        originCoordinate: Coordinate,
        destinationCoordinate: Coordinate
    ): Fare? {
        val response =
            kakaoRouteFeignClient.getRoute(
                "${originCoordinate.lon},${originCoordinate.lat}",
                "${destinationCoordinate.lon},${destinationCoordinate.lat}"
            )
        return response.firstTaxiFare()
    }
}
