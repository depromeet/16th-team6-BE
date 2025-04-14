package com.deepromeet.atcha.app.infrastructure

import com.deepromeet.atcha.app.domain.AppVersion
import com.deepromeet.atcha.app.domain.AppVersionRepository
import org.springframework.data.jpa.repository.JpaRepository

interface AppVersionJpaRepository : AppVersionRepository, JpaRepository<AppVersion, Long>
