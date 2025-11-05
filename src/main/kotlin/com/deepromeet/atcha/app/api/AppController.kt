package com.deepromeet.atcha.app.api

import com.deepromeet.atcha.app.api.request.AppVersionUpdateRequest
import com.deepromeet.atcha.app.application.AppService
import com.deepromeet.atcha.app.domain.Platform
import com.deepromeet.atcha.shared.web.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/app")
class AppController(
    private val appService: AppService
) {
    @GetMapping("/version")
    fun getAppVersion(
        @RequestHeader("X-Platform") platform: String
    ): ApiResponse<String> = ApiResponse.success(appService.getAppVersion(Platform.fromString(platform)).version)

    @PostMapping("/version")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateAppVersion(
        @RequestBody request: AppVersionUpdateRequest,
        @RequestHeader("X-Platform") platform: String
    ) = appService.updateAppVersion(Platform.fromString(platform), request.version)
}
