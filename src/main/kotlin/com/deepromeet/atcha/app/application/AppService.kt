package com.deepromeet.atcha.app.application

import com.deepromeet.atcha.app.domain.AppVersion
import com.deepromeet.atcha.app.domain.Platform
import org.springframework.stereotype.Service

@Service
class AppService(
    private val appVersionAppender: AppVersionAppender,
    private val appVersionReader: AppVersionReader
) {
    fun getAppVersion(platform: Platform): AppVersion = appVersionReader.getAppVersion(platform)

    fun updateAppVersion(
        platform: Platform,
        version: String
    ) {
        appVersionAppender.updateAppVersion(platform, version)
    }
}
