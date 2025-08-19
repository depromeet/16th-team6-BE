package com.deepromeet.atcha.mixpanel

import com.deepromeet.atcha.mixpanel.event.MixpanelEventProperty

class MixpanelEvent(
    val mixpanelEventName: MixpanelEventName,
    val distinctId: String,
    val property: MixpanelEventProperty
)
