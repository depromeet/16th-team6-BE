package com.deepromeet.seulseul.common.token

import com.deepromeet.seulseul.common.web.exception.RequestException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

private const val AUTHORIZATION = "Authorization"

private const val TOKEN_TYPE = "Bearer "

@Component
class CurrentUserArgumentResolver(
    private val tokenGenerator: TokenGenerator
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java) &&
                parameter.parameterType == Long::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw RequestException.NoRequestInfo
        val authorization = request.getHeader(AUTHORIZATION)
        if (authorization == null || !authorization.startsWith(TOKEN_TYPE)) {
            throw RequestException.NotValidHeader
        }
        val token = authorization.substring(TOKEN_TYPE.length)
        tokenGenerator.validateAccessToken(token)
        return tokenGenerator.getUserIdByToken(token)
    }
}
