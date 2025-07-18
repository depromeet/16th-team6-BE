package com.deepromeet.atcha.user.application

import com.deepromeet.atcha.auth.application.UserProviderAppender
import com.deepromeet.atcha.auth.domain.Provider
import com.deepromeet.atcha.auth.domain.SignUpInfo
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserUpdateInfo
import com.deepromeet.atcha.user.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserAppender(
    private val userJpaRepository: UserJpaRepository,
    private val userProviderAppender: UserProviderAppender
) {
    fun append(user: User): User = userJpaRepository.save(user)

    fun append(
        provider: Provider,
        signUpInfo: SignUpInfo
    ): User {
        val user =
            User(
                providerId = provider.providerUserId,
                address = signUpInfo.getAddress(),
                alertFrequencies = signUpInfo.alertFrequencies.toMutableSet(),
                fcmToken = signUpInfo.fcmToken
            )
        val saved = userJpaRepository.save(user)
        userProviderAppender.append(saved, provider)
        return saved
    }

    fun update(
        user: User,
        userUpdateInfo: UserUpdateInfo
    ): User {
        userUpdateInfo.alertFrequencies?.let { user.alertFrequencies = it }
        userUpdateInfo.address?.let { user.address.address = it }
        userUpdateInfo.lat?.let { user.address.lat = it }
        userUpdateInfo.lon?.let { user.address.lon = it }
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
