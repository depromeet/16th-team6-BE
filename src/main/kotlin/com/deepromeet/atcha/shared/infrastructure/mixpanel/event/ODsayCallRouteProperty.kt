package com.deepromeet.atcha.shared.infrastructure.mixpanel.event

class ODsayCallRouteProperty(
    val serviceRegion: String,
    val startStationName: String,
    val endStationName: String
) : MixpanelEventProperty
