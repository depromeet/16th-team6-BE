package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.route.exception.UserRouteError
import com.deepromeet.atcha.route.exception.UserRouteException
import com.deepromeet.atcha.user.domain.User
import org.springframework.stereotype.Component

@Component
class UserRouteManager(
    private val userRouteRepository: UserRouteRepository
) {
    fun update(
        user: User,
        route: LastRoute
    ) {
        val userRoute =
            UserRoute(
                token = user.fcmToken,
                departureTime = route.departureDateTime,
                routeId = route.id,
                userId = user.id
            )
        userRouteRepository.save(userRoute)
    }

    fun update(userRoute: UserRoute) = userRouteRepository.save(userRoute)

    fun read(user: User): UserRoute {
        return userRouteRepository.findById(user.id)
            ?: throw UserRouteException.of(
                UserRouteError.USER_ROUTE_NOT_FOUND,
                "id(${user.id}) 유저가 등록한 경로를 찾을 수 없습니다."
            )
    }

    fun readAll(): List<UserRoute> = userRouteRepository.findAll()

    fun delete(user: User) = userRouteRepository.delete(user.id)
}
