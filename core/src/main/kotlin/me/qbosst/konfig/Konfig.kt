package me.qbosst.konfig

import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapEntrySerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.qbosst.konfig.engine.SerializationEngine
import me.qbosst.konfig.util.getSerialName
import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

open class Konfig<E: Any>(
    path: String,
    override val serializationEngine: SerializationEngine<*, E>,
): Configurable<E>() {
    private val file: File = File(path)
    private var isInitialised: Boolean = false

    fun init() {
        isInitialised = true

        if(!file.exists()) {
            file.createNewFile()

            map = mutableMapOf<String, E>().apply {
                for((prop, delegate) in getConfigProperties()) {
                    delegate.default
                    put(prop.getSerialName(), delegate.default)
                }
            }

            write()
            return
        }

        map = read().toMutableMap()

        @Suppress("UNCHECKED_CAST")
        runCatching { validate() }.onFailure { t ->
            if(t is ConfigPropertiesMissingException) {
                t.properties.forEach { propName ->
                    val prop = getProperty(propName)!! as KProperty1<Configurable<*>, *>

                    prop.isAccessible = true
                    val delegate = prop.getDelegate(this) as DelegatedConfigProperty<out Any?, out Any, *, E, *>
                    prop.isAccessible = false
                    map[propName] = delegate.default
                }

                write()
                return
            }
            throw t
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun write() {
        val content = serializationEngine.engine.encodeToString(
            MapSerializer(String::class.serializer(), serializationEngine.elementSerializer), map
        )

        file.writeText(content)
    }

    @OptIn(InternalSerializationApi::class)
    fun read(): Map<String, E> {
        val content = file.readText()

        return serializationEngine.engine.decodeFromString(
            MapSerializer(String::class.serializer(), serializationEngine.elementSerializer), content
        )
    }

    fun validate() {
        require(isInitialised) { "You must initialise the config first using .init()" }

        val invalid = mutableListOf<String>()
        for((prop, delegate) in getConfigProperties()) {
            if (delegate !is RequiredConfigProperty<out Any, *, E, *>) continue

            val propName = prop.getSerialName()

            if (propName !in map) {
                invalid += propName
                continue
            }
        }

        if(invalid.isNotEmpty()) {
            throw ConfigPropertiesMissingException(invalid)
        }
    }
}

