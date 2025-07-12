package com.deepromeet.atcha.app.domain

import com.deepromeet.atcha.app.exception.AppException
import com.deepromeet.atcha.app.infrastructure.AppVersionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AppVersionReader(
    private val appVersionRepository: AppVersionRepository
) {
    @Transactional(readOnly = true)
    fun getAppVersion(): AppVersion =
        appVersionRepository.findByPlatform(Platform.ANDROID)
            ?: throw AppException.NoMatchedPlatForm
}
