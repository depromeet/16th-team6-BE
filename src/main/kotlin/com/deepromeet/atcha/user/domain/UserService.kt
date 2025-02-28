package com.deepromeet.atcha.user.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userReader: UserReader,
    private val userAppender: UserAppender
) {
    @Transactional(readOnly = true)
    fun getUser(id: Long): User = userReader.read(id)

    @Transactional
    fun updateUser(
        id: Long,
        userUpdateInfo: UserUpdateInfo
    ): User {
        val user = userReader.read(id)
        return userAppender.update(user, userUpdateInfo)
    }

    @Transactional
    fun deleteUser(id: Long) {
        val user = userReader.read(id)
        userAppender.delete(user)
    }
}
