package com.deepromeet.atcha.common.token

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class TokenConfig (
    private val currentUserArgumentResolver: CurrentUserArgumentResolver,
    private val tokenArgumentResolver: TokenArgumentResolver
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
        resolvers.add(tokenArgumentResolver)
    }
}
