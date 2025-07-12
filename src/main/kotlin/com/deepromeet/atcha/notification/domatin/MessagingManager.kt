package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component

@Component
class MessagingManager(
    private val messagingProvider: MessagingProvider
) {
    fun send(messaging: Messaging) {
        messagingProvider.send(messaging)
    }

    fun send(
        notificationContent: NotificationContent,
        token: String
    ) {
        messagingProvider.send(Messaging(notificationContent, token))
    }
}
