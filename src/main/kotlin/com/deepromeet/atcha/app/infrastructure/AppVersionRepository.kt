package com.deepromeet.atcha.app.infrastructure

import com.deepromeet.atcha.app.domain.AppVersion
import com.deepromeet.atcha.app.domain.Platform
import org.springframework.data.jpa.repository.JpaRepository

interface AppVersionRepository : JpaRepository<AppVersion, Long> {
    fun findByPlatform(platform: Platform): AppVersion?
}
