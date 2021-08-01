package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import me.qbosst.konfig.engine.SerializationEngine
import me.qbosst.konfig.properties.*
import me.qbosst.konfig.util.ConfigDefaults
import me.qbosst.konfig.util.getSerialName
import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

open class Konfig<E: Any>(
    val serializationEngine: SerializationEngine<*, E>
) {
    private lateinit var file: File
    internal val map: MutableMap<String, E> = mutableMapOf()

    fun init(path: String) {
        file = File(path)

        if(!file.exists()) {
            file.createNewFile()

            write()
            return
        }

        // write values
        for((key, element) in read()) {
            map[key] = element
        }

        try { validate() } catch (e: ConfigPropertiesMissingException) {
            e.properties.forEach { property ->
                val prop = this::class.memberProperties.first { it.getSerialName() == property } as KProperty1<Konfig<E>, *>
                prop.isAccessible = true
                val delegate = prop.getDelegate(this) as ConfigProperty<out Any, out Any?, E>
                prop.isAccessible = false
                map[property] = delegate.encodeDefaultToElement(serializationEngine)
            }

            write()
        }
    }

    fun write() {
        val content = serializationEngine.encodeToString(
            MapSerializer(String.serializer(), serializationEngine.elementSerializer),
            map
        )

        file.writeText(content)
    }

    fun read(): Map<String, E> {
        val content = file.readText()

        return serializationEngine.decodeFromString(
            MapSerializer(String.serializer(), serializationEngine.elementSerializer),
            content
        )
    }

    fun validate(against: Map<String, E> = read()) {
        val invalid = map.filterNot { (key, _) -> key in against }.keys.toList()

        if(invalid.isNotEmpty()) {
            throw ConfigPropertiesMissingException(invalid)
        }
    }
}

inline fun <reified T: Any, E: Any> Konfig<E>.required(
    default: T = ConfigDefaults[T::class],
    serializer: KSerializer<T> = serializer()
) = DelegatedProvider<T, E, RequiredConfigProperty<T, E>>(serializer, default) { RequiredConfigProperty(serializer, default) }

inline fun <reified T: Any, E: Any> Konfig<E>.defaulting(
    default: T = ConfigDefaults[T::class],
    serializer: KSerializer<T> = serializer()
) = DelegatedProvider<T, E, DefaultingConfigProperty<T, E>>(serializer, default) { DefaultingConfigProperty(serializer, default) }

inline fun <reified T: Any, E: Any> Konfig<E>.optional(
    default: T? = ConfigDefaults[T::class],
    serializer: KSerializer<T> = serializer()
) = DelegatedProvider<T, E, OptionalConfigProperty<T, E>>(serializer, default) { OptionalConfigProperty(serializer, default) }