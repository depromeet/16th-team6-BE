package com.deepromeet.atcha.transit.infrastructure.client.public.config

import com.deepromeet.atcha.transit.infrastructure.client.public.response.GyeonggiBusStation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class BusStationListDeserializer : JsonDeserializer<List<GyeonggiBusStation>>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): List<GyeonggiBusStation> {
        val mapper = p.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(p)

        return when {
            node.isArray -> {
                node.map { mapper.treeToValue(it, GyeonggiBusStation::class.java) }
            }
            node.isObject -> {
                listOf(mapper.treeToValue(node, GyeonggiBusStation::class.java))
            }
            else -> emptyList()
        }
    }
}
