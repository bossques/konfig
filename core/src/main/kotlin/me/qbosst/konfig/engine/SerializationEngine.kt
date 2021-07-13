package me.qbosst.konfig.engine

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat

interface SerializationEngine<ENGINE: StringFormat, ELEMENT: Any> {
    val engine: ENGINE

    val elementNull: ELEMENT
    val elementSerializer: KSerializer<ELEMENT>

    fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String

    fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T

    fun <T> encodeToElement(serializer: SerializationStrategy<T>, value: T): ELEMENT

    fun <T> decodeFromElement(deserializer: DeserializationStrategy<T>, element: ELEMENT): T
}