package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.qbosst.konfig.util.getSerialName
import kotlin.reflect.KProperty

sealed class DelegatedProperty<T>(val serializer: KSerializer<T>, _generate: T) {

    val generate by lazy { Json.encodeToJsonElement(serializer, _generate) }

    abstract operator fun getValue(konfig: Konfig, property: KProperty<*>): T

    open operator fun setValue(konfig: Konfig, property: KProperty<*>, value: T) {
        val jsonElement = Json.encodeToJsonElement(serializer, value)
        konfig.map[property.getSerialName()] = jsonElement
    }
}

class RequiredProperty<T>(serializer: KSerializer<T>, generate: T): DelegatedProperty<T>(serializer, generate) {
    override fun getValue(konfig: Konfig, property: KProperty<*>): T {
        val propName = property.getSerialName()
        val jsonElement = konfig.map[propName] ?: error("Config value '$propName' is not present")
        return Json.decodeFromJsonElement(serializer, jsonElement)
    }
}