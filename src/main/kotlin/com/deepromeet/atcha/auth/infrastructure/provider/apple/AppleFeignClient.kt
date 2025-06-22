package com.deepromeet.atcha.auth.infrastructure.provider.apple

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(
    name = "appleClient",
    url = "\${apple.api.url}",
    configuration = [AppleFeignConfig::class]
)
interface AppleFeignClient {
    @GetMapping("/auth/keys")
    fun getPublicKeys(): ApplePublicKeys
}
