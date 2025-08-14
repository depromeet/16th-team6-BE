package com.deepromeet.atcha.miaxpanel.event

class BusApiCallCountPerRequestProperty(
    var oDsayCallCount: Int = 0,
    var publicBusCallCount: Int = 0,
    var routeName: String
) : MixpanelEventProperty {
    fun incrementODsayCallCount() {
        oDsayCallCount++
    }

    fun incrementPublicBusCallCount() {
        publicBusCallCount++
    }

    fun decrementPublicBusCallCount() {
        if (publicBusCallCount > 0) {
            publicBusCallCount--
        }
    }
}
