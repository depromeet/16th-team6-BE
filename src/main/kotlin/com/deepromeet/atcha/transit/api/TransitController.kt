package com.deepromeet.atcha.transit.api

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.api.request.BusArrivalRequest
import com.deepromeet.atcha.transit.api.request.LastRoutesRequest
import com.deepromeet.atcha.transit.api.request.TaxiFareRequest
import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
import com.deepromeet.atcha.transit.api.response.RealTimeBusArrivalResponse
import com.deepromeet.atcha.transit.domain.Fare
import com.deepromeet.atcha.transit.domain.TransitService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
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

    @GetMapping("/bus-arrival")
    fun getArrivalInfo(
        @ModelAttribute request: BusArrivalRequest
    ): ApiResponse<List<RealTimeBusArrivalResponse>> {
        return ApiResponse.success(
            transitService.getBusArrivalInfo(
                request.routeName,
                request.stationName,
                Coordinate(request.lat, request.lon)
            )?.realTimeInfo?.map { RealTimeBusArrivalResponse(it) } ?: emptyList()
        )
    }

    @GetMapping("/last-routes")
    suspend fun getLastRoutes(
        @CurrentUser id: Long,
        @ModelAttribute request: LastRoutesRequest
    ): ApiResponse<List<LastRoutesResponse>> =
        ApiResponse.success(transitService.getLastRoutes(id, request.toStart(), request.endLat, request.endLon))

    @GetMapping("/last-routes/{routeId}")
    fun getLastRoute(
        @PathVariable routeId: String
    ): ApiResponse<LastRoutesResponse> =
        ApiResponse.success(
            transitService.getRoute(routeId)
        )

    @GetMapping("/last-routes/{routeId}/departure-remaining")
    fun getDepartureRemainingTime(
        @PathVariable routeId: String
    ): ApiResponse<Int> =
        ApiResponse.success(
            transitService.getDepartureRemainingTime(routeId)
        )

    @GetMapping("/batch")
    fun batch(): ApiResponse<Unit> {
        transitService.init()
        return ApiResponse.success(Unit)
    }
}
