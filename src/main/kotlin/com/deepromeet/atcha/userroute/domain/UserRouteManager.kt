package com.deepromeet.atcha.userroute.domain

import com.deepromeet.atcha.transit.domain.route.LastRoute
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

    fun readAll(): List<UserRoute> = userRouteRepository.findAll()

    fun delete(
        userId: Long,
        routeId: String
    ) = userRouteRepository.delete(userId, routeId)
}
