package com.deepromeet.atcha.route.aspect

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.application.LastRouteReader
import com.deepromeet.atcha.user.application.UserReader
import com.deepromeet.atcha.user.domain.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.reflect.jvm.kotlinFunction

@Aspect
@Component
@Order(1)
class RouteCacheAspect(
    private val lastRouteReader: LastRouteReader,
    private val userReader: UserReader
) {
    @Around("@annotation(com.deepromeet.atcha.route.aspect.RouteCache)")
    fun cacheRoute(joinPoint: ProceedingJoinPoint): Any? {
        val args = joinPoint.args

        val userId = UserId(args[0] as Long)
        val start = args[1] as Coordinate
        val end = args[2] as? Coordinate
        val destination = end ?: userReader.read(userId).getHomeCoordinate()

        val returnClassifier =
            (joinPoint.signature as MethodSignature)
                .method.kotlinFunction?.returnType?.classifier

        val cached = runBlocking { lastRouteReader.read(start, destination) }
        cached?.let {
            return when (returnClassifier) {
                Flow::class -> flow { emitAll(cached.asFlow()) }
                else -> it
            }
        }

        return joinPoint.proceed()
    }
}
