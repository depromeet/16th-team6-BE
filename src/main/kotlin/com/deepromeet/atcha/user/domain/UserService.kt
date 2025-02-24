package com.deepromeet.atcha.user.domain

import com.deepromeet.seulseul.user.api.request.UserInfoUpdateRequest
import com.deepromeet.seulseul.user.api.response.UserInfoResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userReader: UserReader
) {
    @Transactional(readOnly = true)
    fun getUserInfo(id: Long) : UserInfoResponse {
        val user = userReader.findById(id)
        return UserInfoResponse.from(user)
    }

    @Transactional
    fun updateUserInfo(id: Long, userInfoUpdateRequest: UserInfoUpdateRequest) : UserInfoResponse {
        val user = userReader.findById(id)
        user.nickname = userInfoUpdateRequest.nickname
        user.profileImageUrl = userInfoUpdateRequest.profileImageUrl
        user.thumbnailImageUrl = userInfoUpdateRequest.thumbnailImageUrl
        user.alertAgreement = userInfoUpdateRequest.alertAgreement
        user.trackingAgreement = userInfoUpdateRequest.trackingAgreement
        return UserInfoResponse.from(user)
    }
}
