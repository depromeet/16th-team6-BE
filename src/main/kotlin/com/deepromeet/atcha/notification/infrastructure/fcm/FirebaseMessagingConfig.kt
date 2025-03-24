package com.deepromeet.atcha.notification.infrastructure.fcm

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FirebaseMessagingConfig(
    private val firebaseApp: FirebaseApp
) {
    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance(firebaseApp)
    }
}
