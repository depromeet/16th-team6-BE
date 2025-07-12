package com.deepromeet.atcha.notification.domatin

interface MessagingProvider {
    fun send(messaging: Messaging): Boolean
}
