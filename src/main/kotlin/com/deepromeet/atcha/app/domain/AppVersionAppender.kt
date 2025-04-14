package com.deepromeet.atcha.app.domain

import com.deepromeet.atcha.app.infrastructure.AppVersionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AppVersionAppender(
    private val appVersionRepository: AppVersionRepository
) {
    @Transactional
    fun createAppVersion(version: String): AppVersion {
        val appVersion = AppVersion(version = version, platform = Platform.ANDROID)
        return appVersionRepository.save(appVersion)
    }

    @Transactional
    fun updateAppVersion(version: String) {
        val appVersion =
            appVersionRepository.findByPlatform(Platform.ANDROID) // TODO 플랫폼 추가 시 수정 필요
                ?: createAppVersion(version)
        appVersion.version = version
    }
}
