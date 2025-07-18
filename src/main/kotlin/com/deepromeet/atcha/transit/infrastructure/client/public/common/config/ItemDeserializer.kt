package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.Items
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.exc.MismatchedInputException

/**
 * Public OpenAPI 응답에서
 * "items": { "item": { ... } } (단건)
 * "items": { "item": [ {...}, {...} ] } (다건)
 * 모두 대응하기 위해 만든 Deserializer 예시
 */
class ItemDeserializer(
    private val valueType: JavaType? = null
) : JsonDeserializer<Items<*>>(), ContextualDeserializer {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): Items<*> {
        val mapper = p.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(p)

        // "item" 필드 자체가 없는 경우도 대비
        val itemNode = node["item"] ?: return Items(null)

        if (valueType == null) {
            // Deserializer에 필요한 타입 정보가 없으면 예외
            throw MismatchedInputException.from(
                p,
                handledType(),
                "ItemDeserializer: 알 수 없는 제네릭 타입입니다. (valueType=null)"
            )
        }

        return if (itemNode.isArray) {
            // JSON 배열이면 리스트로 파싱
            val list = mutableListOf<Any>()
            // List<SubwayStationResponse> 처럼 "List의 원소 타입" 추출
            val contentType =
                valueType.contentType
                    ?: throw MismatchedInputException.from(
                        p,
                        handledType(),
                        "ItemDeserializer: List 형태를 위한 contentType이 필요합니다."
                    )

            itemNode.forEach { elementNode ->
                val parsed = mapper.treeToValue(elementNode, contentType.rawClass)
                list.add(parsed)
            }
            // Items<T>에 리스트를 담아서 반환
            Items(list)
        } else {
            // JSON 객체(단건)면, 단일 객체로 파싱
            // 만약 T가 List<Something>라면 '단일 객체 -> 1건짜리 리스트'로 처리해도 되고,
            // T가 단일 DTO 타입이라면 그대로 파싱해서 반환
            val isListType = valueType.isCollectionLikeType
            if (isListType) {
                // List 타입이라면, itemNode(객체)를 파싱해서 리스트 한 건으로 묶기
                val contentType =
                    valueType.contentType
                        ?: throw MismatchedInputException.from(
                            p,
                            handledType(),
                            "ItemDeserializer: List 형태를 위한 contentType이 필요합니다."
                        )
                val parsed = mapper.treeToValue(itemNode, contentType.rawClass)
                Items(listOf(parsed))
            } else {
                // 단일 객체 타입이라면 그대로 파싱
                val single = mapper.treeToValue(itemNode, valueType.rawClass)
                Items(single)
            }
        }
    }

    /**
     * Jackson이 "필드를 역직렬화해야 하는 상황"에서
     * "이 필드는 Items<무언가>"라는 구체적인 JavaType을 넘겨주면
     * createContextual이 그 정보를 받아서 this.valueType에 주입
     */
    override fun createContextual(
        ctxt: DeserializationContext,
        property: BeanProperty?
    ): JsonDeserializer<*> {
        val wrapperType = ctxt.contextualType // Items<T>의 실제 런타임 타입
        if (wrapperType != null && wrapperType.containedTypeCount() > 0) {
            // Items<T>면 containedType(0)이 T에 해당
            val itemType = wrapperType.containedType(0)
            return ItemDeserializer(itemType)
        }
        return this
    }
}
