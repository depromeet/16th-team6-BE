package com.deepromeet.atcha.userroute.domain

import org.springframework.stereotype.Component

@Component
class UserRouteReader(
    private val userRouteRepository: UserRouteRepository
) {
    fun findAll(): List<UserRoute> = userRouteRepository.findAll()
}
