package com.deepromeet.atcha.notification.infrastructure

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException

@Configuration
class FirebaseConfig {
    @Bean
    fun firebaseApp(): FirebaseApp {
        val serviceAccount =
            javaClass.getResourceAsStream("/firebase-service-account.json")
                ?: throw IOException("Cannot find 'firebase-service-account.json' in resources.")

        val options =
            FirebaseOptions
                .builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

        return FirebaseApp.getApps().firstOrNull() ?: FirebaseApp.initializeApp(options)
    }
}
