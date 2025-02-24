package com.deepromeet.atcha.location.api.controller

import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.location.api.request.POIHistoryRequest
import com.deepromeet.atcha.location.api.request.POISearchRequest
import com.deepromeet.atcha.location.api.response.LocationResponse
import com.deepromeet.atcha.location.api.response.POIResponse
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.LocationService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @GetMapping("/rgeo")
    fun getReverseGeoLabel(
        @ModelAttribute coordinate: Coordinate
    ): ApiResponse<LocationResponse> =
        locationService.getLocation(coordinate).let {
            return ApiResponse.success(LocationResponse.from(it))
        }

    @PostMapping("/histories")
    fun addRecentPOI(
        @RequestBody request: POIHistoryRequest
    ): ApiResponse<Unit> =
        locationService.addPOIHistory(request.toPOI()).let {
            return ApiResponse.success()
        }

    @GetMapping("/histories")
    fun getRecentPOIs(): ApiResponse<List<POIResponse>> =
        locationService.getPOIHistories().let {
            return ApiResponse.success(
                it.map { poi -> POIResponse.from(poi) }
            )
        }

    @DeleteMapping("/history")
    fun removeRecentPOI(
        @RequestBody request: POIHistoryRequest
    ): ApiResponse<Unit> =
        locationService.removePOIHistory(request.toPOI()).let {
            return ApiResponse.success()
        }

    @DeleteMapping("/histories")
    fun clearRecentPOIs(): ApiResponse<Unit> =
        locationService.clearPOIHistories().let {
            return ApiResponse.success()
        }
}
