package com.deepromeet.atcha.miaxpanel.application

import com.deepromeet.atcha.miaxpanel.MixpanelEvent
import com.mixpanel.mixpanelapi.ClientDelivery
import com.mixpanel.mixpanelapi.MessageBuilder
import com.mixpanel.mixpanelapi.MixpanelAPI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MixpanelEventListener(
    @Value("\${mixpanel.token}")
    val token: String,
) {
    @Async
    @EventListener
    fun eventTrack(mixpanelEvent: MixpanelEvent) {

        // 믹스패널 이벤트 메시지 생성
        val messageBuilder: MessageBuilder = MessageBuilder(token)

        // 이벤트 생성
        val sentEvent: org.json.JSONObject? =
            messageBuilder.event(
                mixpanelEvent.distinctId,
                mixpanelEvent.mixpanelEventName.value,
                null
            )

        // 만든 여러 이벤트를 delivery
        val delivery: ClientDelivery = ClientDelivery()
        delivery.addMessage(sentEvent)

        // Mixpanel로 데이터 전송
        val mixpanel: MixpanelAPI = MixpanelAPI()
        mixpanel.deliver(delivery)
    }
}
