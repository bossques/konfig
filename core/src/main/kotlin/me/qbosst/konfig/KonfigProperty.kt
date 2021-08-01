package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import me.qbosst.konfig.engine.SerializationEngine

sealed class KonfigProperty<T: Any?> {
    abstract val name: String
    internal open lateinit var encoded: Any

    val isInitialised get() = ::encoded.isInitialized

    internal abstract fun decodeFromElement(engine: SerializationEngine<*, *>): T

    internal abstract fun encodeToElement(engine: SerializationEngine<*, *>, arg: T)

    internal abstract fun encodeDefault(engine: SerializationEngine<*, *>)

    internal abstract fun getEncoded(engine: SerializationEngine<*, *>): Any
}

class KonfigObjectItem<T: KonfigObject>(override val name: String, var kObj: T) : KonfigProperty<Map<String, Any?>>() {
    @Suppress("UNCHECKED_CAST")
    override fun decodeFromElement(engine: SerializationEngine<*, *>): Map<String, Any?> {
        return kObj.items.mapValues { (_, item) -> item.decodeFromElement(engine) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun encodeToElement(engine: SerializationEngine<*, *>, arg: Map<String, Any?>) {
        encoded = engine.encodeToElement(
            MapSerializer(String.serializer(), engine.elementSerializer) as KSerializer<Map<String, Any>>,
            kObj.items.mapValues { (name, item) ->
                if(!item.isInitialised) (item as KonfigProperty<Any?>).encodeToElement(engine, arg[name])

                item.getEncoded(engine)
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun encodeDefault(engine: SerializationEngine<*, *>) {
        encoded = engine.encodeToElement(
            MapSerializer(String.serializer(), engine.elementSerializer) as KSerializer<Map<String, Any>>,
            kObj.items.mapValues { (_, item) ->
                if(!item.isInitialised) item.encodeDefault(engine)

                item.getEncoded(engine)
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun getEncoded(engine: SerializationEngine<*, *>): Any {
        return engine.encodeToElement(
            MapSerializer(String.serializer(), engine.elementSerializer) as KSerializer<Map<String, Any>>,
            kObj.items.mapValues { (name, item) ->
                item as KonfigProperty<Any?>
                if(!item.isInitialised) item.encodeToElement(engine, kObj.items[name])

                item.encoded
            }
        )
    }
}

class KonfigItem<ARG: Any, ARG_NULL: ARG?>(
    override val name: String,
    val default: ARG_NULL,
    val serializer: KSerializer<ARG>,
    val validator: ((ARG_NULL) -> Unit)? = null
): KonfigProperty<ARG_NULL>() {

    @Suppress("UNCHECKED_CAST")
    override fun decodeFromElement(engine: SerializationEngine<*, *>): ARG_NULL = if(encoded == engine.elementNull) {
        null
    } else {
        (engine as SerializationEngine<*, Any>).decodeFromElement(serializer, encoded)
    } as ARG_NULL

    override fun encodeToElement(engine: SerializationEngine<*, *>, arg: ARG_NULL) {
        encoded = if(arg == null) engine.elementNull else engine.encodeToElement(serializer, arg)
    }

    override fun encodeDefault(engine: SerializationEngine<*, *>) = encodeToElement(engine, default)

    override fun getEncoded(engine: SerializationEngine<*, *>) = encoded
}