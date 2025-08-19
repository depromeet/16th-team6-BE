package com.deepromeet.atcha.shared.infrastructure.mixpanel.application

import com.deepromeet.atcha.shared.infrastructure.mixpanel.MixpanelEvent
import com.deepromeet.atcha.shared.infrastructure.mixpanel.exception.MixpanelError
import com.deepromeet.atcha.shared.infrastructure.mixpanel.exception.MixpanelException
import com.mixpanel.mixpanelapi.ClientDelivery
import com.mixpanel.mixpanelapi.MessageBuilder
import com.mixpanel.mixpanelapi.MixpanelAPI
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component

@Component
@Profile("prod")
@EnableAsync
class MixpanelEventListener(
    @Value("\${mixpanel.token}")
    val token: String
) {
    private val mixpanelAPI: MixpanelAPI = MixpanelAPI()

    @Async
    @EventListener
    fun eventTrack(mixpanelEvent: MixpanelEvent) {
        // 믹스패널 이벤트 메시지 생성
        val messageBuilder = MessageBuilder(token)

        // 이벤트 생성
        val sentEvent: JSONObject? =
            messageBuilder.event(
                mixpanelEvent.distinctId,
                mixpanelEvent.mixpanelEventName.value,
                JSONObject(mixpanelEvent.property)
            )
        val delivery = ClientDelivery().apply { addMessage(sentEvent) }

        try {
            mixpanelAPI.deliver(delivery)
        } catch (e: Exception) {
            throw MixpanelException.of(MixpanelError.MIXPANEL_EVENT_DELIVERY_FAILURE)
        }
    }
}
