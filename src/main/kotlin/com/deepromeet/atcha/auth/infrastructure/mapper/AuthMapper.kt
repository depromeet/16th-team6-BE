package com.deepromeet.atcha.auth.infrastructure.mapper

import com.deepromeet.atcha.auth.domain.Provider
import com.deepromeet.atcha.auth.domain.UserProvider
import com.deepromeet.atcha.auth.domain.UserProviderId
import com.deepromeet.atcha.auth.infrastructure.entity.ProviderEntity
import com.deepromeet.atcha.auth.infrastructure.entity.UserProviderEntity
import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.infrastructure.entity.UserEntity
import org.springframework.stereotype.Component

/**
 * Auth Domain과 Infrastructure Entity 간의 매핑을 담당하는 Mapper
 * 도메인 객체와 JPA 엔티티 간의 변환 로직
 */
@Component
class AuthMapper {
    fun toDomain(entity: ProviderEntity): Provider {
        return Provider(
            providerUserId = entity.providerUserId,
            providerType = entity.providerType,
            providerToken = entity.providerToken
        )
    }

    fun toEntity(domain: Provider): ProviderEntity {
        return ProviderEntity(
            providerUserId = domain.providerUserId,
            providerType = domain.providerType,
            providerToken = domain.providerToken
        )
    }

    fun toDomain(entity: UserProviderEntity): UserProvider {
        return UserProvider(
            id = UserProviderId(entity.id),
            userId = UserId(entity.user.id),
            provider = toDomain(entity.provider)
        )
    }

    fun toEntity(
        domain: UserProvider,
        userEntity: UserEntity
    ): UserProviderEntity {
        return UserProviderEntity(
            id = domain.id.value,
            user = userEntity,
            provider = toEntity(domain.provider)
        )
    }
}
