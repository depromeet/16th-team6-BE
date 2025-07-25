package com.deepromeet.atcha.user.infrastructure.mapper

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.user.domain.HomeAddress
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.infrastructure.entity.AddressEntity
import com.deepromeet.atcha.user.infrastructure.entity.UserEntity
import org.springframework.stereotype.Component

/**
 * Domain과 Infrastructure Entity 간의 매핑을 담당하는 Mapper
 * 도메인 객체와 JPA 엔티티 간의 변환 로직
 */
@Component
class UserMapper {
    /**
     * UserEntity를 User로 변환
     */
    fun toDomain(entity: UserEntity): User {
        return User(
            id = UserId(entity.id),
            providerId = entity.providerId,
            homeAddress = entity.address.toDomain(),
            alertFrequencies = entity.alertFrequencies.toSet(),
            fcmToken = entity.fcmToken,
            isDeleted = entity.isDeleted
        )
    }

    fun toEntity(domain: User): UserEntity {
        return UserEntity(
            id = domain.id.value,
            providerId = domain.providerId,
            address = domain.homeAddress.toEntity(),
            alertFrequencies = domain.alertFrequencies.toMutableSet(),
            fcmToken = domain.fcmToken,
            isDeleted = domain.isDeleted
        )
    }

    fun updateEntity(
        entity: UserEntity,
        domain: User
    ): UserEntity {
        entity.address = domain.homeAddress.toEntity()
        entity.alertFrequencies = domain.alertFrequencies.toMutableSet()
        entity.fcmToken = domain.fcmToken
        entity.isDeleted = domain.isDeleted
        return entity
    }

    private fun AddressEntity.toDomain(): HomeAddress {
        return HomeAddress(
            address = address,
            coordinate = Coordinate(lat = lat, lon = lon)
        )
    }

    private fun HomeAddress.toEntity(): AddressEntity {
        return AddressEntity(
            address = address,
            lat = coordinate.lat,
            lon = coordinate.lon
        )
    }
}
