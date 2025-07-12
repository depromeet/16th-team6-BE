package com.deepromeet.atcha.transit.infrastructure.client.public.config

import com.deepromeet.atcha.transit.infrastructure.client.public.response.GyeonggiBusRoute
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class BusRouteListDeserializer : JsonDeserializer<List<GyeonggiBusRoute>>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): List<GyeonggiBusRoute> {
        val mapper = p.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(p)

        return when {
            node.isArray -> {
                node.map { mapper.treeToValue(it, GyeonggiBusRoute::class.java) }
            }
            node.isObject -> {
                listOf(mapper.treeToValue(node, GyeonggiBusRoute::class.java))
            }
            else -> emptyList()
        }
    }
}
