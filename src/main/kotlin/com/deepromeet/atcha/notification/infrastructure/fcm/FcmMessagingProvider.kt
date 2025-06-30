package com.deepromeet.atcha.notification.infrastructure.fcm

import com.deepromeet.atcha.notification.domatin.Messaging
import com.deepromeet.atcha.notification.domatin.MessagingProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class FcmMessagingProvider(
    private val firebaseMessaging: FirebaseMessaging
) : MessagingProvider {
    private val log = KotlinLogging.logger {}

    override fun send(messaging: Messaging): Boolean {
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
        try {
            firebaseMessaging.send(message)
            return true
        } catch (e: FirebaseMessagingException) {
            log.warn(e) { }
            return false
        } catch (e: Exception) {
            throw e
        }
    }
}
