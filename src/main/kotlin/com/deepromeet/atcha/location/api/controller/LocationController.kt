package com.deepromeet.atcha.location.api.controller

import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.location.api.request.POIHistoryRequest
import com.deepromeet.atcha.location.api.request.POISearchRequest
import com.deepromeet.atcha.location.api.response.POIResponse
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.LocationService
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

    @PostMapping("/histories")
    @ResponseStatus(HttpStatus.CREATED)
    fun addRecentPOI(
        @RequestBody request: POIHistoryRequest
    ) = locationService.addPOIHistory(request.toPOI())

    @GetMapping("/histories")
    fun getRecentPOIs(
        @ModelAttribute coordinate: Coordinate
    ): ApiResponse<List<POIResponse>> =
        locationService.getPOIHistories(coordinate).let {
            return ApiResponse.success(
                it.map { poi -> POIResponse.from(poi) }
            )
        }

    @DeleteMapping("/history")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeRecentPOI(
        @RequestBody request: POIHistoryRequest
    ) = locationService.removePOIHistory(request.toPOI())

    @DeleteMapping("/histories")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun clearRecentPOIs() = locationService.clearPOIHistories()
}
