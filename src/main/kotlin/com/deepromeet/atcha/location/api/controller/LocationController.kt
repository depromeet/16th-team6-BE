package com.deepromeet.atcha.location.api.controller

import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.location.api.request.LocationSearchRequest
import com.deepromeet.atcha.location.api.response.LocationResponse
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
    fun getLocations(
        @ModelAttribute request: LocationSearchRequest
    ): ApiResponse<List<LocationResponse>> =
        locationService.getLocations(
            request.keyword,
            request.toCoordinate()
        ).let {
            return ApiResponse.success(
                it.map { location -> LocationResponse.from(location) }
            )
        }
}
