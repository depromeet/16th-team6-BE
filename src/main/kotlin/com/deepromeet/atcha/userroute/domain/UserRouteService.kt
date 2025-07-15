package com.deepromeet.atcha.userroute.domain

import com.deepromeet.atcha.transit.domain.route.LastRouteReader
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service

@Service
class UserRouteService(
    private val lastRouteReader: LastRouteReader,
    private val userReader: UserReader,
    private val userRouteManager: UserRouteManager
) {
    fun addUserRoute(
        id: Long,
        lastRouteId: String
    ) {
        val user = userReader.read(id)
        val route = lastRouteReader.read(lastRouteId)
        userRouteManager.update(user, route)
    }

    fun deleteUserRoute(
        id: Long,
        lastRouteId: String
    ) {
        val user = userReader.read(id)
        userRouteManager.delete(user.id, lastRouteId)
    }
}
