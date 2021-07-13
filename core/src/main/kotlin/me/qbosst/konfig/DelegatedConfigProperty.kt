package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import me.qbosst.konfig.engine.SerializationEngine
import me.qbosst.konfig.util.getSerialName
import kotlin.reflect.KProperty

sealed class DelegatedConfigProperty<NULLABLE_ARG: ARG?, ARG: Any, FORMAT: StringFormat, ELEMENT: Any, ENGINE: SerializationEngine<FORMAT, ELEMENT>> {
    abstract val serializer: KSerializer<ARG>
    abstract val default: ELEMENT
    abstract val engine: SerializationEngine<FORMAT, ELEMENT>

    abstract operator fun getValue(config: Configurable<ELEMENT>, property: KProperty<*>): NULLABLE_ARG

    abstract operator fun setValue(config: Configurable<ELEMENT>, property: KProperty<*>, value: NULLABLE_ARG)
}

class RequiredConfigProperty<ARG: Any, FORMAT: StringFormat, ELEMENT: Any, ENGINE: SerializationEngine<FORMAT, ELEMENT>>(
    override val serializer: KSerializer<ARG>,
    override val default: ELEMENT,
    override val engine: SerializationEngine<FORMAT, ELEMENT>
): DelegatedConfigProperty<ARG, ARG, FORMAT, ELEMENT, ENGINE>() {

    constructor(
        default: ARG,
        serializer: KSerializer<ARG>,
        engine: SerializationEngine<FORMAT, ELEMENT>
    ): this(
        serializer,
        engine.encodeToElement(serializer, default),
        engine
    )

    override fun getValue(config: Configurable<ELEMENT>, property: KProperty<*>): ARG {
        val propName = property.getSerialName()
        val element = config.map[propName]
            ?: throw ConfigPropertyMissingException("Config value '$propName' is not present")
        return engine.decodeFromElement(serializer, element)
    }

    override fun setValue(config: Configurable<ELEMENT>, property: KProperty<*>, value: ARG) {
        val element = engine.encodeToElement(serializer, value)
        config.map[property.getSerialName()] = element
    }
}

class DefaultingConfigProperty<ARG: Any, FORMAT: StringFormat, ELEMENT: Any, ENGINE: SerializationEngine<FORMAT, ELEMENT>>(
    override val serializer: KSerializer<ARG>,
    override val default: ELEMENT,
    override val engine: SerializationEngine<FORMAT, ELEMENT>
): DelegatedConfigProperty<ARG, ARG, FORMAT, ELEMENT, ENGINE>() {

    constructor(
        default: ARG,
        serializer: KSerializer<ARG>,
        engine: SerializationEngine<FORMAT, ELEMENT>
    ): this(
        serializer,
        engine.encodeToElement(serializer, default),
        engine
    )

    override fun getValue(config: Configurable<ELEMENT>, property: KProperty<*>): ARG {
        val propName = property.getSerialName()
        val element = config.map.getOrPut(propName, ::default)
        return engine.decodeFromElement(serializer, element)
    }

    override fun setValue(config: Configurable<ELEMENT>, property: KProperty<*>, value: ARG) {
        val element = engine.encodeToElement(serializer, value)
        config.map[property.getSerialName()] = element
    }
}

class OptionalConfigProperty<NULLABLE_ARG: ARG?, ARG: Any, FORMAT: StringFormat, ELEMENT: Any, ENGINE: SerializationEngine<FORMAT, ELEMENT>>(
    override val serializer: KSerializer<ARG>,
    override val default: ELEMENT,
    override val engine: SerializationEngine<FORMAT, ELEMENT>
): DelegatedConfigProperty<NULLABLE_ARG, ARG, FORMAT, ELEMENT, ENGINE>() {
    constructor(
        default: NULLABLE_ARG,
        serializer: KSerializer<ARG>,
        engine: SerializationEngine<FORMAT, ELEMENT>
    ): this(
        serializer,
        if(default == null) engine.elementNull else engine.encodeToElement(serializer, default),
        engine
    )

    @Suppress("UNCHECKED_CAST")
    override fun getValue(config: Configurable<ELEMENT>, property: KProperty<*>): NULLABLE_ARG {
        val propName = property.getSerialName()
        val element = config.map.getOrPut(propName, ::default)

        return (if(engine.elementNull == element) null else engine.decodeFromElement(serializer, element)) as NULLABLE_ARG
    }

    override fun setValue(config: Configurable<ELEMENT>, property: KProperty<*>, value: NULLABLE_ARG) {
        val element = if(value == null) engine.elementNull else engine.encodeToElement(serializer, value)
        config.map[property.getSerialName()] = element
    }
}