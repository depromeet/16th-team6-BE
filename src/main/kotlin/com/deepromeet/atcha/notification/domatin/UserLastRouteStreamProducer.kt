package com.deepromeet.atcha.notification.domatin

interface UserLastRouteStreamProducer {
    fun produce(
        userLastRoute: UserLastRoute,
        retryCount: Int = 0
    )

    fun produceAll(userLastRoutes: List<UserLastRoute>)

    fun produceToDeadLetter(
        userLastRoute: UserLastRoute,
        retryCount: Int = 3
    )
}
