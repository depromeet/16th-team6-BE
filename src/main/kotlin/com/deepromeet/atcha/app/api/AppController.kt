package com.deepromeet.atcha.app.api

import com.deepromeet.atcha.app.api.request.AppVersionUpdateRequest
import com.deepromeet.atcha.app.application.AppService
import com.deepromeet.atcha.common.web.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/app")
class AppController(
    private val appService: AppService
) {
    @GetMapping("/version")
    fun getAppVersion(): ApiResponse<String> = ApiResponse.success(appService.getAppVersion().version)

    @PostMapping("/version")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateAppVersion(
        @RequestBody request: AppVersionUpdateRequest
    ) = appService.updateAppVersion(request.version)
}
