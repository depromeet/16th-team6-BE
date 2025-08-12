package com.deepromeet.atcha.miaxpanel

class MixpanelEvent(
    val mixpanelEventName: MixpanelEventName,
    val distinctId: String,
) {
    enum class MixpanelEventName(val value: String) {
        ODSAY_CALL_COUNT("odsay_call_count"),
    }

}
