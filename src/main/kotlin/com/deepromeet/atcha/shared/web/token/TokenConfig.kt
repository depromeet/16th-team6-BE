package com.deepromeet.atcha.shared.web.token

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class TokenConfig(
    private val currentUserArgumentResolver: CurrentUserArgumentResolver,
    private val tokenArgumentResolver: TokenArgumentResolver
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(0, currentUserArgumentResolver) // Add at index 0 to prioritize
        resolvers.add(1, tokenArgumentResolver)
    }
}
