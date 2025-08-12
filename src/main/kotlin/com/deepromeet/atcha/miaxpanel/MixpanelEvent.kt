package com.deepromeet.atcha.miaxpanel

import com.deepromeet.atcha.miaxpanel.event.MixpanelEventProperty

class MixpanelEvent(
    val mixpanelEventName: MixpanelEventName,
    val distinctId: String,
    val property: MixpanelEventProperty,
) {
    enum class MixpanelEventName(val value: String) {
        ODSAY_CALL_COUNT("odsay_call_count"),
    }
}
