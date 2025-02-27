package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.user.api.request.UserInfoUpdateRequest
import com.deepromeet.atcha.user.api.response.UserInfoResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userReader: UserReader
) {
    @Transactional(readOnly = true)
    fun getUserInfo(id: Long): UserInfoResponse {
        val user = userReader.read(id)
        return UserInfoResponse.from(user)
    }

    @Transactional
    fun updateUserInfo(
        id: Long,
        userInfoUpdateRequest: UserInfoUpdateRequest
    ): UserInfoResponse {
        val user = userReader.read(id).apply {
            nickname = userInfoUpdateRequest.nickname
            profileImageUrl = userInfoUpdateRequest.profileImageUrl
            agreement.alert = userInfoUpdateRequest.agreement.alert
            agreement.tracking = userInfoUpdateRequest.agreement.tracking
        }
        return UserInfoResponse.from(user)
    }

    @Transactional
    fun deleteUser(id: Long) {
        val user = userReader.read(id)
        user.isDeleted = true
    }
}
