package com.deepromeet.atcha.notification.domain

import org.springframework.stereotype.Component

@Component
class MessagingManager(
    private val messagingProvider: MessagingProvider
) {
    fun send(messaging: Messaging): Boolean = messagingProvider.send(messaging)

    fun send(
        notificationContent: NotificationContent,
        token: String
    ) {
        messagingProvider.send(Messaging(notificationContent, token))
    }
}
