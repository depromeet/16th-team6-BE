package com.deepromeet.atcha.user.infrastructure.repository

import com.deepromeet.atcha.user.infrastructure.entity.UserWithdrawalReasonEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserWithdrawalReasonJpaRepository : JpaRepository<UserWithdrawalReasonEntity, Long>
