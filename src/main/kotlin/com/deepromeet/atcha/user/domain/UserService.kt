package com.deepromeet.atcha.user.domain

import com.deepromeet.seulseul.user.api.response.UserInfoResponse
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userReader: UserReader
) {
    fun getUserInfo(id: Long) : UserInfoResponse {
        val user = userReader.findById(id)
        return UserInfoResponse.from(user)
    }
}
