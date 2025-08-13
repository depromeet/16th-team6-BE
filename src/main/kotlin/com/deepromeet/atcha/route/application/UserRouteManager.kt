package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.route.exception.RouteError
import com.deepromeet.atcha.route.exception.RouteException
import com.deepromeet.atcha.user.domain.User
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
class UserRouteManager(
    private val userRouteRepository: UserRouteRepository
) {
    fun append(
        user: User,
        route: LastRoute
    ) {
        val userRoute =
            UserRoute(
                token = user.fcmToken ?: "",
                departureTime = route.departureDateTime,
                routeId = route.id,
                userId = user.id
            )

        val userRouteExpiresAt =
            route.calculateArrivalTime()
                .plusHours(1)

        userRouteRepository.save(userRoute, Duration.between(LocalDateTime.now(), userRouteExpiresAt))
    }

    fun update(
        userRoute: UserRoute,
        expiresAt: LocalDateTime
    ): UserRoute {
        return userRouteRepository.save(userRoute, Duration.between(LocalDateTime.now(), expiresAt))
    }

    fun read(user: User): UserRoute {
        return userRouteRepository.findById(user.id)
            ?: throw RouteException.of(
                RouteError.USER_ROUTE_NOT_FOUND,
                "id(${user.id.value}) 유저가 등록한 경로를 찾을 수 없습니다."
            )
    }

    fun readAll(): List<UserRoute> = userRouteRepository.findAll()

    fun delete(user: User) = userRouteRepository.delete(user.id)
}
