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
        origin: Coordinate,
        destination: Coordinate
    ): Fare? {
        val response =
            kakaoRouteFeignClient.getRoute(
                "${origin.lon},${origin.lat}",
                "${destination.lon},${destination.lat}"
            )
        return response.firstTaxiFare()
    }
}
