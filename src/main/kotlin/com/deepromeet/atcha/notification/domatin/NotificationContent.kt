package com.deepromeet.atcha.notification.domatin

data class NotificationContent(
    val title: String,
    val body: String,
    val dataMap: Map<String, String>
)
