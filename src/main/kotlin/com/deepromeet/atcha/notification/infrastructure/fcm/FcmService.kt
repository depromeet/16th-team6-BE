package com.deepromeet.atcha.notification.infrastructure.fcm

import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
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

    fun sendNonPush(
        targetToken: String,
        data: Map<String, String>
    ): String {
        val message =
            Message.builder()
                .setToken(targetToken)
                .putAllData(data)
                .build()

        return firebaseMessaging.send(message)
    }

    fun sendSilentPush(
        targetToken: String,
        data: Map<String, String>
    ): String {
        val apnsConfig =
            ApnsConfig.builder()
                .setAps(
                    Aps.builder()
                        .setContentAvailable(true) // 이게 핵심!
                        .setSound("") // 빈 문자열로 소리 제거
                        .build()
                )
                .build()

        val message =
            Message.builder()
                .setToken(targetToken)
                .setApnsConfig(apnsConfig)
                .putAllData(data)
                .build()

        return firebaseMessaging.send(message)
    }
}
