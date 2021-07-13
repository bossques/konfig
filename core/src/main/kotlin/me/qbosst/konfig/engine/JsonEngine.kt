package me.qbosst.konfig.engine

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

class JsonEngine(override val engine: Json): SerializationEngine<Json, JsonElement> {
    override val elementNull: JsonElement = JsonNull

    override val elementSerializer: KSerializer<JsonElement> = JsonElement.serializer()

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        return engine.encodeToString(serializer, value)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return engine.decodeFromString(deserializer, string)
    }

    override fun <T> encodeToElement(serializer: SerializationStrategy<T>, value: T): JsonElement {
        return engine.encodeToJsonElement(serializer, value)
    }

    override fun <T> decodeFromElement(deserializer: DeserializationStrategy<T>, element: JsonElement): T {
        return engine.decodeFromJsonElement(deserializer, element)
    }
}