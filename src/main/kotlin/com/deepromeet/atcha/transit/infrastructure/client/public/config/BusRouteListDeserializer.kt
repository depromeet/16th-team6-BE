package com.deepromeet.atcha.transit.infrastructure.client.public.config

import com.deepromeet.atcha.transit.infrastructure.client.public.response.GyeonggiBusStationRoute
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class BusRouteListDeserializer : JsonDeserializer<List<GyeonggiBusStationRoute>>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): List<GyeonggiBusStationRoute> {
        val mapper = p.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(p)

        return when {
            node.isArray -> {
                node.map { mapper.treeToValue(it, GyeonggiBusStationRoute::class.java) }
            }
            node.isObject -> {
                listOf(mapper.treeToValue(node, GyeonggiBusStationRoute::class.java))
            }
            else -> emptyList()
        }
    }
}
