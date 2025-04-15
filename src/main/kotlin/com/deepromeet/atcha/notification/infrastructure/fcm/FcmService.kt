package com.deepromeet.atcha.notification.infrastructure.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service

@Service
class FcmService(
    private val firebaseMessaging: FirebaseMessaging
) {
    fun send(
        targetToken: String,
        title: String,
        body: String,
        data: Map<String, String>
    ): String {
        val message =
            Message.builder()
                .setToken(targetToken)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )
                .putAllData(data)
                .build()

        return firebaseMessaging.send(message)
    }
}
