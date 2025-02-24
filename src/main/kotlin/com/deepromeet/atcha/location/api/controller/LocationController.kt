package com.deepromeet.atcha.location.api.controller

import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.location.api.request.POISearchRequest
import com.deepromeet.atcha.location.api.response.POIResponse
import com.deepromeet.atcha.location.domain.LocationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
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
        locationService.getPOIs(
            request.keyword,
            request.toCoordinate()
        ).let {
            return ApiResponse.success(
                it.map { poi -> POIResponse.from(poi) }
            )
        }
}
