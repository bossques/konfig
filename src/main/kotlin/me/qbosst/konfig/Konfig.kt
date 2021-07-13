@file:OptIn(InternalSerializationApi::class)

package me.qbosst.konfig

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import me.qbosst.konfig.util.ConfigDefaults
import me.qbosst.konfig.util.getSerialName
import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

open class Konfig(path: String) {
    init {
        require(path.endsWith(".json")) { "Path must point to a json file!" }
    }

    internal lateinit var map: MutableMap<String, JsonElement>
    private val file: File = File(path)
    private var isInitialised: Boolean = false

    fun init() {
        isInitialised = true

        if(!file.exists()) {
            file.createNewFile()

            map = mutableMapOf<String, JsonElement>().apply {
                for((prop, delegate) in getConfigProperties()) {
                    put(prop.getSerialName(), delegate.default)
                }
            }

            write()
            return
        }

        map = Json.decodeFromString(file.readText())

        @Suppress("UNCHECKED_CAST")
        runCatching { validate() }.onFailure { t ->
            if(t is ConfigPropertiesMissingException) {
                t.properties.forEach { propName ->
                    val prop = getProperty(propName)!! as KProperty1<Konfig, *>

                    prop.isAccessible = true
                    val delegate = prop.getDelegate(this) as DelegatedConfigProperty<out Any?, out Any>
                    prop.isAccessible = false
                    map[propName] = delegate.default
                }

                write()
                return
            }
            throw t
        }
    }

    fun write() {
        file.writeText(Json.encodeToString(map))
    }

    fun validate() {
        require(isInitialised) { "You must initialise the config first using .init()" }

        val invalid = mutableListOf<String>()
        for((prop, delegate) in getConfigProperties()) {
            if (delegate !is RequiredConfigProperty<out Any>) continue

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

    @Suppress("UNCHECKED_CAST")
    private fun getConfigProperties() = this::class.memberProperties.mapNotNull {
        val prop = it as KProperty1<Konfig, *>
        prop.isAccessible = true
        val delegate = prop.getDelegate(this) as? DelegatedConfigProperty<out Any?, out Any> ?: return@mapNotNull null
        prop.isAccessible = false

        return@mapNotNull prop to delegate
    }

    private fun getProperty(name: String) = this::class.memberProperties.firstOrNull { it.getSerialName() == name }
}

inline fun <reified T: Any> Konfig.required(
    default: T = ConfigDefaults[T::class],
    serializer: KSerializer<T> = T::class.serializer()
): RequiredConfigProperty<T> = RequiredConfigProperty(serializer, default)

inline fun <reified T: Any> Konfig.required(
    default: JsonElement,
    serializer: KSerializer<T> = T::class.serializer()
): RequiredConfigProperty<T> = RequiredConfigProperty(serializer, default)

inline fun <reified T: Any> Konfig.defaulting(
    default: T,
    serializer: KSerializer<T> = T::class.serializer()
): DefaultingConfigProperty<T> = DefaultingConfigProperty(serializer, default)

inline fun <reified T: Any> Konfig.defaulting(
    default: JsonElement,
    serializer: KSerializer<T> = T::class.serializer()
): DefaultingConfigProperty<T> = DefaultingConfigProperty(serializer, default)

inline fun <reified T: Any> Konfig.optional(
    default: T? = null,
    serializer: KSerializer<T> = T::class.serializer()
): OptionalConfigProperty<T?, T> = OptionalConfigProperty(serializer, default)

inline fun <reified T: Any> Konfig.optional(
    default: JsonElement,
    serializer: KSerializer<T> = T::class.serializer()
): OptionalConfigProperty<T?, T> = OptionalConfigProperty(serializer, default)

