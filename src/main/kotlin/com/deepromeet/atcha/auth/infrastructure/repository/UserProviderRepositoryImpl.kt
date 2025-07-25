package com.deepromeet.atcha.auth.infrastructure.repository

import com.deepromeet.atcha.auth.domain.UserProvider
import com.deepromeet.atcha.auth.domain.UserProviderId
import com.deepromeet.atcha.auth.domain.UserProviderRepository
import com.deepromeet.atcha.auth.infrastructure.mapper.AuthMapper
import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.infrastructure.repository.UserEntityJpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class UserProviderRepositoryImpl(
    private val userProviderEntityJpaRepository: UserProviderEntityJpaRepository,
    private val userEntityJpaRepository: UserEntityJpaRepository,
    private val authMapper: AuthMapper
) : UserProviderRepository {
    override fun save(userProvider: UserProvider): UserProvider {
        val userEntity =
            userEntityJpaRepository.findById(userProvider.userId.value)
                .orElseThrow { IllegalArgumentException("User not found with id: ${userProvider.userId.value}") }

        val entity = authMapper.toEntity(userProvider, userEntity)
        val savedEntity = userProviderEntityJpaRepository.save(entity)
        return authMapper.toDomain(savedEntity)
    }

    @Transactional(readOnly = true)
    override fun findByUserId(userId: UserId): UserProvider? {
        return userProviderEntityJpaRepository.findByUserId(userId.value)
            ?.let { authMapper.toDomain(it) }
    }

    @Transactional(readOnly = true)
    override fun findById(id: UserProviderId): UserProvider? {
        return userProviderEntityJpaRepository.findById(id.value)
            .map { authMapper.toDomain(it) }
            .orElse(null)
    }

    override fun delete(userProvider: UserProvider) {
        userProviderEntityJpaRepository.deleteById(userProvider.id.value)
    }

    @Transactional(readOnly = true)
    override fun existsByUserId(userId: UserId): Boolean {
        return userProviderEntityJpaRepository.findByUserId(userId.value) != null
    }
}
