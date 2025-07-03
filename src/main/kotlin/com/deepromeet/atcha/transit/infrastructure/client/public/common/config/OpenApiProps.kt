package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "open-api")
class OpenApiProps {
    lateinit var api: ApiSection
    lateinit var limits: LimitSection

    class ApiSection {
        lateinit var url: Map<String, String>
    }

    class LimitSection {
        var default: Int = 10
        var perApi: Map<String, Int> = emptyMap()
    }
}
