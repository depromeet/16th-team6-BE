package com.deepromeet.atcha.transit.api

import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.transit.api.request.BusArrivalRequest
import com.deepromeet.atcha.transit.api.request.TaxiFareRequest
import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.Fare
import com.deepromeet.atcha.transit.domain.TransitService
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.TMapRouteResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transits")
class TransitController(
    private val transitService: TransitService
) {
    @GetMapping("/test")
    fun test(): ApiResponse<TMapRouteResponse> {
        return ApiResponse.success(transitService.getRoutes())
    }

    @GetMapping("/taxi-fare")
    fun getTaxiFare(
        @ModelAttribute request: TaxiFareRequest
    ): ApiResponse<Fare> =
        ApiResponse.success(
            transitService.getTaxiFare(
                request.toStart(),
                request.toEnd()
            )
        )

    @GetMapping("/arrival")
    fun getArrivalInfo(
        @ModelAttribute request: BusArrivalRequest
    ): ApiResponse<BusArrival> {
        return ApiResponse.success(
            transitService.getBusArrivalInfo(
                request.routeName,
                request.stationName
            )
        )
    }
}
