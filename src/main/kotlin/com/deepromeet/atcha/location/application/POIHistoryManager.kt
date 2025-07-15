package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.user.domain.User
import org.springframework.stereotype.Component

@Component
class POIHistoryManager(
    private val poiHistoryQueue: POIHistoryQueue
) {
    fun append(
        user: User,
        poi: POI
    ) {
        if (poiHistoryQueue.exists(user, poi)) {
            poiHistoryQueue.pop(user, poi)
        }
        poiHistoryQueue.push(user, poi)
    }

    fun getAll(user: User): List<POI> {
        return poiHistoryQueue.getAll(user)
    }

    fun clear(user: User) {
        poiHistoryQueue.popAll(user)
    }

    fun remove(
        user: User,
        poi: POI
    ) {
        poiHistoryQueue.pop(user, poi)
    }
}
