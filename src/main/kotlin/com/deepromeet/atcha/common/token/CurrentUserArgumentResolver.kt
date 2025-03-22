package com.deepromeet.atcha.common.token

import com.deepromeet.atcha.common.web.exception.RequestException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class CurrentUserArgumentResolver(
    private val tokenGenerator: TokenGenerator
) : HandlerMethodArgumentResolver {
    companion object {
        private const val TOKEN_TYPE = "Bearer "
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentUser::class.java) &&
            parameter.parameterType == Long::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Long {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw RequestException.NoRequestInfo
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authorization == null || !authorization.startsWith(TOKEN_TYPE)) {
            throw RequestException.NotValidHeader
        }
        val token = authorization.substring(TOKEN_TYPE.length)
        tokenGenerator.validateToken(token, TokenType.ACCESS)
        return tokenGenerator.getUserIdByToken(token, TokenType.ACCESS)
    }
}
