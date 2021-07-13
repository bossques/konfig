package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.encodeToJsonElement
import me.qbosst.konfig.util.getSerialName
import kotlin.reflect.KProperty

sealed class DelegatedConfigProperty<T>(
    val serializer: KSerializer<T>
) {
    abstract val default: JsonElement

    abstract operator fun getValue(konfig: Konfig, property: KProperty<*>): T

    open operator fun setValue(konfig: Konfig, property: KProperty<*>, value: T) {
        val jsonElement = Json.encodeToJsonElement(serializer, value)
        konfig.map[property.getSerialName()] = jsonElement
    }
}

class RequiredConfigProperty<T>(
    serializer: KSerializer<T>,
    override val default: JsonElement
): DelegatedConfigProperty<T>(serializer) {

    constructor(serializer: KSerializer<T>, default: T): this(serializer, Json.encodeToJsonElement(serializer, default))

    override fun getValue(konfig: Konfig, property: KProperty<*>): T {
        val propName = property.getSerialName()
        val jsonElement = konfig.map[propName] ?: throw ConfigException("Config value '$propName' is not present")
        return Json.decodeFromJsonElement(serializer, jsonElement)
    }
}

class DefaultingConfigProperty<T>(
    serializer: KSerializer<T>,
    override val default: JsonElement
): DelegatedConfigProperty<T>(serializer) {
    constructor(serializer: KSerializer<T>, default: T): this(serializer, Json.encodeToJsonElement(serializer, default))

    override fun getValue(konfig: Konfig, property: KProperty<*>): T {
        val propName = property.getSerialName()
        val jsonElement = konfig.map.getOrPut(propName, ::default)
        return Json.decodeFromJsonElement(serializer, jsonElement)
    }
}