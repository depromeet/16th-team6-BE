package com.deepromeet.atcha.transit.infrastructure.client.kakao

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.Fare
import com.deepromeet.atcha.transit.domain.TaxiFareFetcher
import org.springframework.stereotype.Component

@Component
class KakaoTaxiFareClient(
    private val kakaoRouteFeignClient: KakaoRouteFeignClient
) : TaxiFareFetcher {
    override fun fetch(
        start: Coordinate,
        end: Coordinate
    ): Fare? {
        val response =
            kakaoRouteFeignClient.getRoute(
                "${start.lon},${start.lat}",
                "${end.lon},${end.lat}"
            )
        return response.firstTaxiFare()
    }
}
