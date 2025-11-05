package com.deepromeet.atcha.support.fixture

import com.deepromeet.atcha.auth.domain.ProviderContext
import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType
import com.deepromeet.atcha.user.domain.User

object ProviderFixture {
    fun create(user: User) = ProviderContext(user.providerId, ProviderType.KAKAO, "testProviderToken")
}
