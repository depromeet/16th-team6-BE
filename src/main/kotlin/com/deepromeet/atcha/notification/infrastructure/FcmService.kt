package com.deepromeet.atcha.notification.infrastructure

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service

@Service
class FcmService(
    private val firebaseMessaging: FirebaseMessaging
) {
    fun sendMessageTo(
        targetToken: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
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
