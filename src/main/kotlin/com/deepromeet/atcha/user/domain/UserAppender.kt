package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.auth.domain.SignUpInfo
import com.deepromeet.atcha.auth.infrastructure.response.ProviderUserInfoResponse
import com.deepromeet.atcha.user.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserAppender(
    private val userJpaRepository: UserJpaRepository
) {
    fun save(user: User): User = userJpaRepository.save(user)

    fun save(
        providerUserInfo: ProviderUserInfoResponse,
        signUpInfo: SignUpInfo
    ): User {
        val user =
            User(
                nickname = providerUserInfo.nickname,
                providerId = providerUserInfo.providerId,
                profileImageUrl = providerUserInfo.profileImageUrl,
                address = signUpInfo.getAddress(),
                alertFrequencies = signUpInfo.alertFrequencies.toMutableSet(),
                fcmToken = signUpInfo.fcmToken
            )
        return userJpaRepository.save(user)
    }

    fun update(
        user: User,
        userUpdateInfo: UserUpdateInfo
    ): User {
        userUpdateInfo.nickname?.let { user.nickname = it }
        userUpdateInfo.profileImageUrl?.let { user.profileImageUrl = it }
        userUpdateInfo.alertFrequencies?.let { user.alertFrequencies = it }
        userUpdateInfo.address?.let { user.address.address = it }
        userUpdateInfo.lat?.let { user.address.lat = it }
        userUpdateInfo.log?.let { user.address.lon = it }
        userUpdateInfo.fcmToken?.let { user.fcmToken = it }
        return user
    }

    fun updateFcmToken(
        user: User,
        fcmToken: String
    ): User {
        if (user.fcmToken != fcmToken) {
            user.fcmToken = fcmToken
        }
        return user
    }

    fun delete(user: User) {
        user.isDeleted = true
    }
}
