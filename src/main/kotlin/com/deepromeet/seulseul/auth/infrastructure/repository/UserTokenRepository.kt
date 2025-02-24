package com.deepromeet.seulseul.auth.infrastructure.repository

import com.deepromeet.seulseul.auth.domain.UserToken
import org.springframework.data.jpa.repository.JpaRepository

interface UserTokenRepository : JpaRepository<UserToken, Long>
