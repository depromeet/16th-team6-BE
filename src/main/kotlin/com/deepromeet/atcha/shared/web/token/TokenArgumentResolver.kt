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
class TokenArgumentResolver : HandlerMethodArgumentResolver {
    companion object {
        private const val TOKEN_TYPE = "Bearer "
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(Token::class.java) &&
            parameter.parameterType == String::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): String {
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
        return authorization.substring(TOKEN_TYPE.length)
    }
}
