package com.deepromeet.atcha.notification.domain

interface NotificationDuplicateChecker {
    fun isNewNotification(idempotencyKey: String): Boolean

    fun markAsFailed(idempotencyKey: String)
}
