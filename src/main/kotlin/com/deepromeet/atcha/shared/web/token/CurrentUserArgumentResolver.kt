package com.deepromeet.atcha.shared.web.token

import com.deepromeet.atcha.shared.web.exception.RequestError
import com.deepromeet.atcha.shared.web.exception.RequestException
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
    private val jwtTokeParser: JwtTokeParser,
    private val tokenExpirationManager: TokenExpirationManager
) : HandlerMethodArgumentResolver {
    companion object {
        private const val TOKEN_TYPE = "Bearer "
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java) &&
            parameter.parameterType == Long::class.javaPrimitiveType
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw RequestException.Companion.of(RequestError.NO_REQUEST_INFO, "HTTP 요청 정보를 가져올 수 없습니다")
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authorization == null || !authorization.startsWith(TOKEN_TYPE)) {
            throw RequestException.Companion.of(
                RequestError.NOT_VALID_HEADER,
                "Authorization 헤더가 없거나 Bearer 토큰 형식이 아닙니다"
            )
        }
        val token = authorization.substring(TOKEN_TYPE.length)
        tokenExpirationManager.validateNotExpired(token)
        jwtTokeParser.validateToken(token, TokenType.ACCESS)
        val userId = jwtTokeParser.getUserId(token, TokenType.ACCESS)
        return userId.value
    }
}
