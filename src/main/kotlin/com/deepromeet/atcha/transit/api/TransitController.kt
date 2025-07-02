package com.deepromeet.atcha.transit.api

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.transit.api.request.BusArrivalRequest
import com.deepromeet.atcha.transit.api.request.BusRouteRequest
import com.deepromeet.atcha.transit.api.request.LastRoutesRequest
import com.deepromeet.atcha.transit.api.request.TaxiFareRequest
import com.deepromeet.atcha.transit.api.response.BusArrivalResponse
import com.deepromeet.atcha.transit.api.response.BusRoutePositionResponse
import com.deepromeet.atcha.transit.api.response.LastRouteResponse
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.Fare
import com.deepromeet.atcha.transit.domain.LastRoute
import com.deepromeet.atcha.transit.domain.TransitService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
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

    @GetMapping("/last-routes")
    suspend fun getLastRoutes(
        @CurrentUser id: Long,
        @ModelAttribute request: LastRoutesRequest
    ): ApiResponse<List<LastRouteResponse>> =
        ApiResponse.success(
            transitService.getLastRoutes(
                id,
                request.toStart(),
                request.toEnd(),
                request.sortType
            ).map { LastRouteResponse(it) }
        )

    @GetMapping("/last-routes/{routeId}")
    fun getLastRoute(
        @PathVariable routeId: String
    ): ApiResponse<LastRoute> =
        ApiResponse.success(
            transitService.getRoute(routeId)
        )

    @PostMapping("/bus-arrival")
    fun getArrivalInfo(
        @RequestBody request: BusArrivalRequest
    ): ApiResponse<BusArrivalResponse> {
        return ApiResponse.success(
            BusArrivalResponse(
                transitService.getBusArrival(
                    request.toRouteName(),
                    request.toBusStationMeta(),
                    request.toPassStopList()
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
    fun getBusOperationInfo(
        @ModelAttribute request: BusRouteRequest
    ): ApiResponse<BusRouteOperationInfo> {
        return ApiResponse.success(
            transitService.getBusOperationInfo(request.toBusRoute())
        )
    }

    @GetMapping("/last-routes/{routeId}/departure-remaining")
    fun getDepartureRemainingTime(
        @PathVariable routeId: String
    ): ApiResponse<Int> =
        ApiResponse.success(
            transitService.getDepartureRemainingTime(routeId)
        )

    @GetMapping("/last-routes/{lastRouteId}/bus-started")
    suspend fun isBusStarted(
        @PathVariable lastRouteId: String
    ): ApiResponse<Boolean> =
        ApiResponse.success(
            transitService.isBusStarted(lastRouteId)
        )

    @GetMapping("/batch")
    fun batch(): ApiResponse<Unit> {
        transitService.init()
        return ApiResponse.success(Unit)
    }
}
