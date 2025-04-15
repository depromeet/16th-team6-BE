package com.deepromeet.atcha.notification.domatin

interface UserNotificationStreamProducer {
    fun produce(
        userNotification: UserNotification,
        retryCount: Int = 0
    )

    fun produceAll(userNotifications: List<UserNotification>)

    fun produceToDeadLetter(
        userNotification: UserNotification,
        retryCount: Int = 3
    )
}
