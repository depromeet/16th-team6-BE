package com.deepromeet.atcha.user.application

import com.deepromeet.atcha.user.domain.HomeAddress
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserUpdateInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userReader: UserReader,
    private val userAppender: UserAppender,
    private val userUpdater: UserUpdater
) {
    @Transactional(readOnly = true)
    fun getUser(id: Long): User = userReader.read(id)

    @Transactional
    fun updateUser(
        id: Long,
        userUpdateInfo: UserUpdateInfo
    ): User {
        val user = userReader.read(id)
        return userUpdater.update(user, userUpdateInfo)
    }

    @Transactional
    fun updateAlertFrequency(
        id: Long,
        alertFrequency: MutableSet<Int>
    ): User {
        val user = userReader.read(id)
        return userUpdater.updateAlertFrequency(user, alertFrequency)
    }

    @Transactional
    fun updateHomeAddress(
        id: Long,
        homeAddress: HomeAddress
    ): User {
        val user = userReader.read(id)
        return userUpdater.updateHomeAddress(user, homeAddress)
    }

    @Transactional
    fun deleteUser(id: Long) {
        val user = userReader.read(id)
        userAppender.delete(user)
    }
}
