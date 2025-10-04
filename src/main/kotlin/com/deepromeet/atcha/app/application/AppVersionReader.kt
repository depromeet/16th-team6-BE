package com.deepromeet.atcha.app.application

import com.deepromeet.atcha.app.domain.AppVersion
import com.deepromeet.atcha.app.domain.Platform
import com.deepromeet.atcha.app.exception.AppError
import com.deepromeet.atcha.app.exception.AppException
import com.deepromeet.atcha.app.infrastructure.AppVersionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AppVersionReader(
    private val appVersionRepository: AppVersionRepository
) {
    @Transactional(readOnly = true)
    fun getAppVersion(platform: Platform): AppVersion =
        appVersionRepository.findByPlatform(platform)
            ?: throw AppException.of(AppError.NO_MATCHED_PLATFORM, "Android 플랫폼 앱 버전을 찾을 수 없습니다")
}
