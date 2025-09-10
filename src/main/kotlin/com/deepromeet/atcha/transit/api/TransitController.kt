package com.deepromeet.atcha.transit.api

import com.deepromeet.atcha.shared.web.ApiResponse
import com.deepromeet.atcha.transit.api.request.BusArrivalRequest
import com.deepromeet.atcha.transit.api.request.BusRouteRequest
import com.deepromeet.atcha.transit.api.request.TaxiFareRequest
import com.deepromeet.atcha.transit.api.response.BusArrivalResponse
import com.deepromeet.atcha.transit.api.response.BusRoutePositionResponse
import com.deepromeet.atcha.transit.application.TransitService
import com.deepromeet.atcha.transit.domain.Fare
import com.deepromeet.atcha.transit.domain.bus.BusRouteOperationInfo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transits")
class TransitController(
    private val transitService: TransitService
) {
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

    @PostMapping("/bus-arrival")
    suspend fun getArrivalInfo(
        @RequestBody request: BusArrivalRequest
    ): ApiResponse<BusArrivalResponse> {
        return ApiResponse.success(
            BusArrivalResponse(
                transitService.getBusArrival(
                    request.toRouteName(),
                    request.toBusStationMeta(),
                    request.toRoutePassStops()
                )
            )
        )
    }

    @GetMapping("/bus-routes/positions")
    suspend fun getBusRoutePositions(
        @ModelAttribute request: BusRouteRequest
    ): ApiResponse<BusRoutePositionResponse> {
        return ApiResponse.success(
            BusRoutePositionResponse(transitService.getBusPositions(request.toBusRoute()))
        )
    }

    @GetMapping("/bus-routes/operation-info")
    suspend fun getBusOperationInfo(
        @ModelAttribute request: BusRouteRequest
    ): ApiResponse<BusRouteOperationInfo> {
        return ApiResponse.success(
            transitService.getBusOperationInfo(request.toBusRoute())
        )
    }

    @GetMapping("/batch")
    suspend fun batch(): ApiResponse<Unit> {
        transitService.init()
        return ApiResponse.success(Unit)
    }
}
