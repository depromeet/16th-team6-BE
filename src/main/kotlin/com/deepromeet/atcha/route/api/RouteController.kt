package com.deepromeet.atcha.route.api

import com.deepromeet.atcha.route.api.request.LastRoutesRequest
import com.deepromeet.atcha.route.api.request.UserRouteRequest
import com.deepromeet.atcha.route.api.response.LastRouteResponse
import com.deepromeet.atcha.route.api.response.UserRouteResponse
import com.deepromeet.atcha.route.application.RouteService
import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.shared.web.ApiResponse
import com.deepromeet.atcha.shared.web.token.CurrentUser
import com.deepromeet.atcha.user.domain.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/routes")
class RouteController(
    private val routeService: RouteService
) {
    @GetMapping("/last-routes")
    suspend fun getLastRoutes(
        @CurrentUser id: Long,
        @ModelAttribute request: LastRoutesRequest
    ): ApiResponse<List<LastRouteResponse>> =
        ApiResponse.success(
            routeService.getLastRoutes(
                UserId(id),
                request.toStart(),
                request.toEnd(),
                request.sortType
            ).map { LastRouteResponse(it) }
        )

    @GetMapping("/v2/last-routes")
    suspend fun getLastRoutesV2(
        @CurrentUser id: Long,
        @ModelAttribute request: LastRoutesRequest
    ): ApiResponse<List<LastRouteResponse>> =
        ApiResponse.success(
            routeService.getLastRoutesV2(
                UserId(id),
                request.toStart(),
                request.toEnd(),
                request.sortType
            ).map { LastRouteResponse(it) }
        )

    @GetMapping(
        "/v3/last-routes/stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun streamLastRoutesV3(
        @CurrentUser id: Long,
        @ModelAttribute request: LastRoutesRequest
    ): Flow<LastRouteResponse> =
        routeService.getLastRouteStream(
            UserId(id),
            request.toStart(),
            request.toEnd(),
            request.sortType
        ).map { LastRouteResponse(it) }

    @GetMapping("/last-routes/{routeId}")
    fun getLastRoute(
        @PathVariable routeId: String
    ): ApiResponse<LastRoute> =
        ApiResponse.success(
            routeService.getRoute(routeId)
        )

    @GetMapping("/last-routes/{lastRouteId}/bus-started")
    suspend fun isBusStarted(
        @PathVariable lastRouteId: String
    ): ApiResponse<Boolean> =
        ApiResponse.success(
            routeService.isBusStarted(lastRouteId)
        )

    @GetMapping("/last-routes/{routeId}/departure-remaining")
    fun getDepartureRemainingTime(
        @PathVariable routeId: String
    ): ApiResponse<Int> =
        ApiResponse.success(
            routeService.getDepartureRemainingTime(routeId)
        )

    @PostMapping("/user-routes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addUserRoute(
        @CurrentUser id: Long,
        @RequestBody request: UserRouteRequest
    ) {
        routeService.addUserRoute(UserId(id), request.lastRouteId)
    }

    @DeleteMapping("/user-routes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUserRoute(
        @CurrentUser id: Long,
        @ModelAttribute request: UserRouteRequest
    ) {
        routeService.deleteUserRoute(UserId(id))
    }

    @GetMapping("/user-routes/refresh")
    suspend fun refreshUserRoute(
        @CurrentUser id: Long
    ): ApiResponse<UserRouteResponse> =
        ApiResponse.success(
            UserRouteResponse(
                routeService.refreshUserRoute(UserId(id))
            )
        )
}
