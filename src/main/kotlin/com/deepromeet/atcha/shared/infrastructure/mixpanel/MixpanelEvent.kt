package com.deepromeet.atcha.shared.infrastructure.mixpanel

import com.deepromeet.atcha.shared.infrastructure.mixpanel.event.MixpanelEventProperty

class MixpanelEvent(
    val mixpanelEventName: MixpanelEventName,
    val distinctId: String,
    val property: MixpanelEventProperty
)
