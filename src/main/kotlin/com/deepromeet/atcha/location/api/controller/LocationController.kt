package com.deepromeet.atcha.location.api.controller

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.location.api.request.POIHistoryRequest
import com.deepromeet.atcha.location.api.request.POISearchRequest
import com.deepromeet.atcha.location.api.response.LocationResponse
import com.deepromeet.atcha.location.api.response.POIResponse
import com.deepromeet.atcha.location.application.LocationService
import com.deepromeet.atcha.location.domain.Coordinate
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/locations")
class LocationController(
    private val locationService: LocationService
) {
    @GetMapping
    fun getPOIs(
        @ModelAttribute request: POISearchRequest
    ): ApiResponse<List<POIResponse>> =
        ApiResponse.success(
            locationService.getPOIs(request.keyword, request.toCoordinate())
                .map(POIResponse::from)
        )

    @GetMapping("/rgeo")
    fun getReverseGeoLabel(
        @ModelAttribute coordinate: Coordinate
    ): ApiResponse<LocationResponse> =
        ApiResponse.success(
            LocationResponse(locationService.getLocation(coordinate))
        )

    @PostMapping("/histories")
    @ResponseStatus(HttpStatus.CREATED)
    fun addRecentPOI(
        @CurrentUser userId: Long,
        @RequestBody request: POIHistoryRequest
    ) = locationService.addPOIHistory(
        userId,
        request.toPOI()
    )

    @GetMapping("/histories")
    fun getRecentPOIs(
        @CurrentUser userId: Long,
        @ModelAttribute coordinate: Coordinate
    ): ApiResponse<List<POIResponse>> =
        locationService.getPOIHistories(userId, coordinate).let {
            return ApiResponse.success(
                it.map { poi -> POIResponse.from(poi) }
            )
        }

    @DeleteMapping("/history")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeRecentPOI(
        @CurrentUser userId: Long,
        @ModelAttribute request: POIHistoryRequest
    ) = locationService.removePOIHistory(userId, request.toPOI())

    @DeleteMapping("/histories")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun clearRecentPOIs(
        @CurrentUser userId: Long
    ) = locationService.clearPOIHistories(userId)
}
