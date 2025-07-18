package com.deepromeet.atcha.notification.application

import com.deepromeet.atcha.notification.domain.Messaging
import org.springframework.stereotype.Component

@Component
class MessagingManager(
    private val messagingProvider: MessagingProvider
) {
    fun send(messaging: Messaging): Boolean = messagingProvider.send(messaging)
}
