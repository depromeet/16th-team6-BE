package com.deepromeet.atcha.app.domain

import org.springframework.stereotype.Service

@Service
class AppService(
    private val appVersionAppender: AppVersionAppender,
    private val appVersionReader: AppVersionReader
) {
    fun getAppVersion(): AppVersion = appVersionReader.getAppVersion()

    fun updateAppVersion(version: String) {
        appVersionAppender.updateAppVersion(version)
    }
}
