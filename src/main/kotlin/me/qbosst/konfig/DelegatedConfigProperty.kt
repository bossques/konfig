package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.qbosst.konfig.util.getSerialName
import kotlin.reflect.KProperty

sealed class DelegatedConfigProperty<T>(val serializer: KSerializer<T>, _default: T) {

    val default by lazy { Json.encodeToJsonElement(serializer, _default) }

    abstract operator fun getValue(konfig: Konfig, property: KProperty<*>): T

    open operator fun setValue(konfig: Konfig, property: KProperty<*>, value: T) {
        val jsonElement = Json.encodeToJsonElement(serializer, value)
        konfig.map[property.getSerialName()] = jsonElement
    }
}

class RequiredConfigProperty<T>(
    serializer: KSerializer<T>,
    generate: T
): DelegatedConfigProperty<T>(serializer, generate) {
    override fun getValue(konfig: Konfig, property: KProperty<*>): T {
        val propName = property.getSerialName()
        val jsonElement = konfig.map[propName] ?: error("Config value '$propName' is not present")
        return Json.decodeFromJsonElement(serializer, jsonElement)
    }
}

class DefaultingConfigProperty<T>(serializer: KSerializer<T>, default: T): DelegatedConfigProperty<T>(serializer, default) {
    override fun getValue(konfig: Konfig, property: KProperty<*>): T {
        val propName = property.getSerialName()
        val jsonElement = konfig.map.getOrPut(propName, ::default)
        return Json.decodeFromJsonElement(serializer, jsonElement)
    }
}