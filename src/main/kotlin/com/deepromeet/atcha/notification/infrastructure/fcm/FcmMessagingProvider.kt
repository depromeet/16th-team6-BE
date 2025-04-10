package com.deepromeet.atcha.notification.infrastructure.fcm

import com.deepromeet.atcha.notification.domatin.Messaging
import com.deepromeet.atcha.notification.domatin.MessagingProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Component

@Component
class FcmMessagingProvider(
    private val firebaseMessaging: FirebaseMessaging
) : MessagingProvider {
    override fun send(messaging: Messaging): String {
        val message =
            Message.builder()
                .setToken(messaging.token)
                .setNotification(
                    Notification.builder()
                        .setTitle(messaging.title)
                        .setBody(messaging.body)
                        .build()
                )
                .putAllData(messaging.dataMap)
                .build()

        return firebaseMessaging.send(message)
    }
}
