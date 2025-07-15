package com.deepromeet.atcha.userroute.domain

interface UserRouteProducer {
    fun produce(
        userRoute: UserRoute,
        retryCount: Int = 0
    )

    fun produceAll(userRoutes: List<UserRoute>)

    fun produceToDeadLetter(
        userRoute: UserRoute,
        retryCount: Int = 3
    )
}
