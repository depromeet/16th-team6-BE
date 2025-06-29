package com.deepromeet.atcha.transit.infrastructure.client.kakao

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.Fare
import com.deepromeet.atcha.transit.domain.TaxiFareFetcher
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import org.springframework.stereotype.Component

@Component
class KakaoTaxiFareClient(
    private val kakaoRouteFeignClient: KakaoRouteFeignClient
) : TaxiFareFetcher {
    override fun fetch(
        start: Coordinate,
        end: Coordinate
    ): Fare {
        val response =
            kakaoRouteFeignClient.getRoute(
                "${start.lon},${start.lat}",
                "${end.lon},${end.lat}"
            )

        return response.firstTaxiFare() ?: throw TransitException.of(
            TransitError.TAXI_FARE_FETCH_FAILED,
            "출발지(${start.lat}, ${start.lon})에서 도착지(${end.lat}, ${end.lon})까지의 택시 요금을 조회할 수 없습니다."
        )
    }
}
