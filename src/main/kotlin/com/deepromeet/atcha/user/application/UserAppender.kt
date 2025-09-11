package com.deepromeet.atcha.user.application

import com.deepromeet.atcha.auth.application.UserProviderAppender
import com.deepromeet.atcha.auth.domain.ProviderContext
import com.deepromeet.atcha.auth.domain.SignUpInfo
import com.deepromeet.atcha.user.domain.User
import org.springframework.stereotype.Component

@Component
class UserAppender(
    private val userRepository: UserRepository,
    private val userProviderAppender: UserProviderAppender
) {
    fun append(user: User): User = userRepository.save(user)

    fun append(
        providerContext: ProviderContext,
        signUpInfo: SignUpInfo
    ): User {
        val homeAddress = signUpInfo.getAddress()

        val user =
            User.create(
                providerId = providerContext.providerUserId,
                homeAddress = homeAddress,
                alertFrequencies = signUpInfo.alertFrequencies.toSet(),
                fcmToken = signUpInfo.fcmToken
            )

        val saved = userRepository.save(user)
        userProviderAppender.append(saved, providerContext)
        return saved
    }
}
