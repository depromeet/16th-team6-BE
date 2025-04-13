package com.deepromeet.atcha.notification.domatin

interface UserNotificationStreamProducer {
    fun produce(userNotification: UserNotification)

    fun produceAll(userNotifications: List<UserNotification>)
}
