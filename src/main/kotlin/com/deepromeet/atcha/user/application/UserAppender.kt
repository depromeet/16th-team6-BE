package com.deepromeet.atcha.user.application

import com.deepromeet.atcha.auth.application.UserProviderAppender
import com.deepromeet.atcha.auth.domain.Provider
import com.deepromeet.atcha.auth.domain.SignUpInfo
import com.deepromeet.atcha.user.domain.HomeAddress
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.domain.UserRepository
import com.deepromeet.atcha.user.domain.UserUpdateInfo
import org.springframework.stereotype.Component

@Component
class UserAppender(
    private val userRepository: UserRepository,
    private val userProviderAppender: UserProviderAppender
) {
    fun append(user: User): User = userRepository.save(user)

    fun append(
        provider: Provider,
        signUpInfo: SignUpInfo
    ): User {
        val homeAddress = signUpInfo.getAddress()

        val user =
            User.create(
                id = UserId(0L),
                providerId = provider.providerUserId,
                homeAddress = homeAddress,
                alertFrequencies = signUpInfo.alertFrequencies.toSet(),
                fcmToken = signUpInfo.fcmToken
            )

        val saved = userRepository.save(user)
        userProviderAppender.append(saved, provider)
        return saved
    }

    fun update(
        user: User,
        userUpdateInfo: UserUpdateInfo
    ): User {
        var updatedUser = user

        userUpdateInfo.alertFrequencies?.let {
            updatedUser = updatedUser.updateAlertFrequencies(it)
        }

        if (userUpdateInfo.address != null && userUpdateInfo.lat != null && userUpdateInfo.lon != null) {
            val newAddress =
                HomeAddress(
                    address = userUpdateInfo.address,
                    latitude = userUpdateInfo.lat,
                    longitude = userUpdateInfo.lon
                )
            updatedUser = updatedUser.updateHomeAddress(newAddress)
        }

        userUpdateInfo.fcmToken?.let {
            updatedUser = updatedUser.updateFcmToken(it)
        }

        return userRepository.save(updatedUser)
    }

    fun updateFcmToken(
        user: User,
        fcmToken: String
    ): User {
        return if (user.fcmToken != fcmToken) {
            val updatedUser = user.updateFcmToken(fcmToken)
            userRepository.save(updatedUser)
        } else {
            user
        }
    }

    fun delete(user: User) {
        val deletedUser = user.markAsDeleted()
        userRepository.save(deletedUser)
    }
}
