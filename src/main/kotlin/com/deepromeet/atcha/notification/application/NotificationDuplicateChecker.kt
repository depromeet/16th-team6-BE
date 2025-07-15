package com.deepromeet.atcha.notification.application

interface NotificationDuplicateChecker {
    fun isNewNotification(idempotencyKey: String): Boolean

    fun markAsFailed(idempotencyKey: String)
}
