package me.qbosst.konfig.properties

import kotlinx.serialization.KSerializer
import me.qbosst.konfig.ConfigPropertyMissingException
import me.qbosst.konfig.Konfig
import me.qbosst.konfig.engine.SerializationEngine
import me.qbosst.konfig.util.getSerialName
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class ConfigProperty<ARG: Any, ARG_NULL: ARG?, ELEMENT: Any>: ReadWriteProperty<Konfig<ELEMENT>, ARG_NULL> {
    abstract val serializer: KSerializer<ARG>
    abstract val default: ARG?

    fun encodeDefaultToElement(engine: SerializationEngine<*, ELEMENT>) = if(default == null) {
        engine.elementNull
    } else {
        engine.encodeToElement(serializer, default!!)
    }
}

class RequiredConfigProperty<ARG: Any, ELEMENT: Any>(
    override val serializer: KSerializer<ARG>,
    override val default: ARG,
): ConfigProperty<ARG, ARG, ELEMENT>() {
    override fun getValue(thisRef: Konfig<ELEMENT>, property: KProperty<*>): ARG {
        val name = property.getSerialName()
        val element = thisRef.map[name]
            ?: throw ConfigPropertyMissingException(name)
        return thisRef.serializationEngine.decodeFromElement(serializer, element)
    }

    override fun setValue(thisRef: Konfig<ELEMENT>, property: KProperty<*>, value: ARG) {
        val element = thisRef.serializationEngine.encodeToElement(serializer, value)
        thisRef.map[property.getSerialName()] = element
    }
}

class DefaultingConfigProperty<ARG: Any, ELEMENT: Any>(
    override val serializer: KSerializer<ARG>,
    override val default: ARG,
): ConfigProperty<ARG, ARG, ELEMENT>() {
    override fun getValue(thisRef: Konfig<ELEMENT>, property: KProperty<*>): ARG {
        val name = property.getSerialName()
        val element = thisRef.map.getOrPut(name) {
            thisRef.serializationEngine.encodeToElement(serializer, default)
        }
        return thisRef.serializationEngine.decodeFromElement(serializer, element)
    }

    override fun setValue(thisRef: Konfig<ELEMENT>, property: KProperty<*>, value: ARG) {
        val element = thisRef.serializationEngine.encodeToElement(serializer, value)
        thisRef.map[property.getSerialName()] = element
    }
}

class OptionalConfigProperty<ARG: Any, ELEMENT: Any>(
    override val serializer: KSerializer<ARG>,
    override val default: ARG?,
): ConfigProperty<ARG, ARG?, ELEMENT>() {
    override fun getValue(thisRef: Konfig<ELEMENT>, property: KProperty<*>): ARG? {
        val name = property.getSerialName()
        val element = thisRef.map.getOrPut(name) {
            if(default == null) thisRef.serializationEngine.elementNull else thisRef.serializationEngine.encodeToElement(serializer, default)
        }
        return if(thisRef.serializationEngine.elementNull == element) null else thisRef.serializationEngine.decodeFromElement(serializer, element)
    }

    override fun setValue(thisRef: Konfig<ELEMENT>, property: KProperty<*>, value: ARG?) {
        val element = if(value == null) thisRef.serializationEngine.elementNull else thisRef.serializationEngine.encodeToElement(serializer, value)
        thisRef.map[property.getSerialName()] = element
    }
}