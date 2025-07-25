package com.deepromeet.atcha.user.infrastructure.repository

import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.domain.UserRepository
import com.deepromeet.atcha.user.infrastructure.mapper.UserMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * UserRepository의 Infrastructure Layer 구현체
 * JPA를 사용한 데이터 접근 로직
 */
@Repository
@Transactional
class UserRepositoryImpl(
    private val jpaRepository: UserEntityJpaRepository,
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
        val entity =
            if (user.id.value == 0L) {
                // 새로운 사용자 생성
                userMapper.toEntity(user)
            } else {
                // 기존 사용자 업데이트
                val existingEntity =
                    jpaRepository.findById(user.id.value)
                        .orElseThrow { IllegalArgumentException("User not found: ${user.id.value}") }
                userMapper.updateEntity(existingEntity, user)
            }

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

    @Transactional(readOnly = true)
    override fun findActiveUsers(
        offset: Int,
        limit: Int
    ): List<User> {
        val pageable = PageRequest.of(offset / limit, limit)
        return jpaRepository.findByIsDeletedFalse(pageable)
            .map { userMapper.toDomain(it) }
    }

    @Transactional(readOnly = true)
    override fun findActiveUsersByAlertFrequency(frequency: Int): List<User> {
        return jpaRepository.findActiveUsersByAlertFrequency(frequency)
            .map { userMapper.toDomain(it) }
    }
}
