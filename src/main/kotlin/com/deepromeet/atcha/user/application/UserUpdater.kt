package com.deepromeet.atcha.user.application

import com.deepromeet.atcha.user.domain.HomeAddress
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserRepository
import com.deepromeet.atcha.user.domain.UserUpdateInfo
import org.springframework.stereotype.Component

@Component
class UserUpdater(
    private val userRepository: UserRepository
) {
    fun update(
        user: User,
        userUpdateInfo: UserUpdateInfo
    ): User {
        val updatedUser = user.update(userUpdateInfo)
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

    fun updateAlertFrequency(
        user: User,
        alertFrequency: MutableSet<Int>
    ): User {
        val updatedUser = user.updateAlertFrequencies(alertFrequency)
        return userRepository.save(updatedUser)
    }

    fun updateHomeAddress(
        user: User,
        homeAddress: HomeAddress
    ): User {
        val updatedUser = user.updateHomeAddress(homeAddress)
        return userRepository.save(updatedUser)
    }
}
