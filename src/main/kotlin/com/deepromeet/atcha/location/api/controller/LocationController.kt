package com.deepromeet.atcha.location.api.controller

import com.deepromeet.atcha.common.dto.Cursor
import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.common.web.SliceResponse
import com.deepromeet.atcha.location.api.request.LocationSearchRequest
import com.deepromeet.atcha.location.api.response.LocationResponse
import com.deepromeet.atcha.location.api.response.LocationSearchResponse
import com.deepromeet.atcha.location.domain.LocationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/locations")
class LocationController(
    private val locationService: LocationService
) {
    @GetMapping
    fun getLocations(
        @RequestBody request: LocationSearchRequest,
        @ModelAttribute cursor: Cursor
    ): ApiResponse<LocationSearchResponse> =
        locationService.getLocations(
            request.keyword,
            request.toCoordinate(),
            cursor
        ).let {
            return ApiResponse.success(
                LocationSearchResponse(SliceResponse.from(it.map { location -> LocationResponse.from(location) }))
            )
        }
}
