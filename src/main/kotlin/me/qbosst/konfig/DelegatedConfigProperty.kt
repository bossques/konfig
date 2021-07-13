package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import me.qbosst.konfig.util.getSerialName
import kotlin.reflect.KProperty

sealed class DelegatedConfigProperty<T: E?, E: Any> {
    abstract val serializer: KSerializer<E>
    abstract val default: JsonElement

    abstract operator fun getValue(konfig: Konfig, property: KProperty<*>): T

    abstract operator fun setValue(konfig: Konfig, property: KProperty<*>, value: T)
}

class RequiredConfigProperty<T: Any>(
    override val serializer: KSerializer<T>,
    override val default: JsonElement
): DelegatedConfigProperty<T, T>() {
    constructor(serializer: KSerializer<T>, default: T): this(serializer, Json.encodeToJsonElement(serializer, default))

    override fun getValue(konfig: Konfig, property: KProperty<*>): T {
        val propName = property.getSerialName()
        val jsonElement = konfig.map[propName]
            ?: throw ConfigPropertyMissingException("Config value '$propName' is not present")
        return Json.decodeFromJsonElement(serializer, jsonElement)
    }

    override fun setValue(konfig: Konfig, property: KProperty<*>, value: T) {
        val jsonElement = Json.encodeToJsonElement(serializer, value)
        konfig.map[property.getSerialName()] = jsonElement
    }
}

class DefaultingConfigProperty<T: Any>(
    override val serializer: KSerializer<T>,
    override val default: JsonElement
): DelegatedConfigProperty<T, T>() {
    constructor(serializer: KSerializer<T>, default: T): this(serializer, Json.encodeToJsonElement(serializer, default))

    override fun getValue(konfig: Konfig, property: KProperty<*>): T {
        val propName = property.getSerialName()
        val jsonElement = konfig.map.getOrPut(propName, ::default)
        return Json.decodeFromJsonElement(serializer, jsonElement)
    }

    override fun setValue(konfig: Konfig, property: KProperty<*>, value: T) {
        val jsonElement = Json.encodeToJsonElement(serializer, value)
        konfig.map[property.getSerialName()] = jsonElement
    }
}

class OptionalConfigProperty<T: E?, E: Any>(
    override val serializer: KSerializer<E>,
    override val default: JsonElement
): DelegatedConfigProperty<T, E>() {
    constructor(serializer: KSerializer<E>, default: T): this(
        serializer,
        if(default == null) JsonNull else Json.encodeToJsonElement(serializer, default)
    )

    @Suppress("UNCHECKED_CAST")
    override fun getValue(konfig: Konfig, property: KProperty<*>): T {
        val propName = property.getSerialName()
        val jsonElement = konfig.map.getOrPut(propName, ::default)

        return (if(jsonElement is JsonNull) null else Json.decodeFromJsonElement(serializer, jsonElement)) as T
    }

    override fun setValue(konfig: Konfig, property: KProperty<*>, value: T) {
        val jsonElement = if(value == null) JsonNull else Json.encodeToJsonElement(serializer, value)
        konfig.map[property.getSerialName()] = jsonElement
    }
}