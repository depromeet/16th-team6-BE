package com.deepromeet.atcha.user.infrastructure.repository

import com.deepromeet.atcha.user.application.UserRepository
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.domain.UserWithdrawalReason
import com.deepromeet.atcha.user.infrastructure.mapper.UserMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class UserRepositoryImpl(
    private val jpaRepository: UserEntityJpaRepository,
    private val userWithdrawalReasonJpaRepository: UserWithdrawalReasonJpaRepository,
    private val userMapper: UserMapper
) : UserRepository {
    @Transactional(readOnly = true)
    override fun findById(userId: UserId): User? {
        return jpaRepository.findByIdAndIsDeletedFalse(userId.value)
            ?.let { userMapper.toDomain(it) }
    }

    @Transactional(readOnly = true)
    override fun findByProviderId(providerId: String): User? {
        return jpaRepository.findByProviderIdAndIsDeletedFalse(providerId)
            ?.let { userMapper.toDomain(it) }
    }

    @Transactional(readOnly = true)
    override fun existsByProviderId(providerId: String): Boolean {
        return jpaRepository.existsByProviderIdAndIsDeletedFalse(providerId)
    }

    override fun save(user: User): User {
        val entity = userMapper.toEntity(user)
        val savedEntity = jpaRepository.save(entity)
        return userMapper.toDomain(savedEntity)
    }

    override fun delete(userId: UserId) {
        val entity =
            jpaRepository.findById(userId.value)
                .orElseThrow { IllegalArgumentException("User not found: ${userId.value}") }
        entity.isDeleted = true
        jpaRepository.save(entity)
    }

    override fun saveWithdrawalReason(reason: UserWithdrawalReason) {
        val entity = userMapper.toEntity(reason)
        userWithdrawalReasonJpaRepository.save(entity)
    }
}
