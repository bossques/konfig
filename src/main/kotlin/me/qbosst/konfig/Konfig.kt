@file:OptIn(InternalSerializationApi::class)

package me.qbosst.konfig

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
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
        validate()
    }

    fun write() {
        file.writeText(Json.encodeToString(map))
    }

    fun validate() {
        require(isInitialised) { "You must initialise the config first using .init()" }

        val invalid = mutableListOf<String>()
        for((prop, delegate) in getConfigProperties()) {
            if (delegate !is RequiredConfigProperty) continue

            val propName = prop.getSerialName()

            if (propName !in map) {
                invalid += propName
                continue
            }
        }

        if(invalid.isNotEmpty()) {
            throw IllegalStateException("The following config properties are missing: $invalid")
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun getConfigProperties() = this::class.memberProperties.mapNotNull {
        val prop = it as KProperty1<Konfig, *>
        prop.isAccessible = true
        val delegate = prop.getDelegate(this) as? DelegatedConfigProperty<*> ?: return@mapNotNull null
        prop.isAccessible = false

        return@mapNotNull prop to delegate
    }
}

/**
 * Creates a required config property.
 *
 * @param generate what to set this value as when generating a new config
 */
inline fun <reified T: Any> Konfig.required(
    generate: T
): RequiredConfigProperty<T> {
    return RequiredConfigProperty(T::class.serializer(), generate)
}

/**
 * Creates a defaulting config property.
 *
 * In the case of a config property not being present, it will use the [default] value
 *
 * @param default the default value that will be used if this config property is missing. This value will also be used
 * when a new config is generated.
 */
inline fun <reified T: Any> Konfig.defaulting(
    default: T
): DelegatedConfigProperty<T> {
    return DefaultingConfigProperty(T::class.serializer(), default)
}
