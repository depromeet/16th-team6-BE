package com.deepromeet.atcha.shared.aspect

import com.deepromeet.atcha.route.infrastructure.cache.LastRouteMetricsRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Aspect
@Component
class MetricsAspect(
    private val metricsRepository: LastRouteMetricsRepository
) {

    @Around("execution(* com.deepromeet.atcha.route.application.LastRouteCalculator.streamLastRoutes(..)) || execution(* com.deepromeet.atcha.route.application.LastRouteCalculator.calcLastRoutes(..))")
    fun collectTotalMetrics(joinPoint: ProceedingJoinPoint): Any? {
        val args = joinPoint.args
        val itineraries = args.getOrNull(2) as? List<*>
        val totalCount = itineraries?.size?.toLong() ?: 0L

        metricsRepository.incrTotal(totalCount)

        return joinPoint.proceed()
    }

    @Around("execution(* com.deepromeet.atcha.route.application.LastRouteAppender.appendRoutes(..))")
    fun collectSuccessMetrics(joinPoint: ProceedingJoinPoint): Any? {
        val args = joinPoint.args
        val routes = args.getOrNull(2) as? List<*>
        val successCount = routes?.size?.toLong() ?: 0L

        metricsRepository.incrSuccess(successCount)

        return joinPoint.proceed()
    }
}
