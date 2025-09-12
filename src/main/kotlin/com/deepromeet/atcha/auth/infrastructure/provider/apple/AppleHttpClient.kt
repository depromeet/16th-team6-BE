package com.deepromeet.atcha.auth.infrastructure.provider.apple

import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface AppleHttpClient {
    @GetExchange("/auth/keys")
    suspend fun getPublicKeys(): ApplePublicKeys
}
