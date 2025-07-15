package com.deepromeet.atcha.notification.domain

data class NotificationContent(
    val title: String,
    val body: String,
    val dataMap: Map<String, String> = emptyMap()
)
