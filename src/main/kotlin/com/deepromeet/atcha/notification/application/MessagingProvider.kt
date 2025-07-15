package com.deepromeet.atcha.notification.application

import com.deepromeet.atcha.notification.domain.Messaging

interface MessagingProvider {
    fun send(messaging: Messaging): Boolean
}
