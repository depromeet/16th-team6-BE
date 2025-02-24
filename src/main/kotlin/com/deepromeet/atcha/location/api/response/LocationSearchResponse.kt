package com.deepromeet.atcha.location.api.response

import com.deepromeet.atcha.common.web.SliceResponse

data class LocationSearchResponse(
    val pois: SliceResponse<LocationResponse>
)
