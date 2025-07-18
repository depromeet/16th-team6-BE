package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.user.domain.User

interface POIHistoryQueue {
    fun push(
        user: User,
        poi: POI
    )

    fun pop(
        user: User,
        poi: POI
    )

    fun popAll(user: User)

    fun getAll(user: User): List<POI>

    fun exists(
        user: User,
        poi: POI
    ): Boolean
}
