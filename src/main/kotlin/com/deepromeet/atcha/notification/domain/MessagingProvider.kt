package com.deepromeet.atcha.notification.domain

interface MessagingProvider {
    fun send(messaging: Messaging): Boolean
}
