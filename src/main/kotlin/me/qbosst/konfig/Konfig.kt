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

    fun init() {
        if(!file.exists()) {
            file.createNewFile()

            map = mutableMapOf<String, JsonElement>().apply {
                for((prop, delegate) in getConfigProperties()) {
                    put(prop.getSerialName(), delegate.generate)
                }
            }

            file.writeText(Json.encodeToString(map))
            return
        }

        map = Json.decodeFromString(file.readText())
    }

    fun write() {
        file.writeText(Json.encodeToString(map))
    }

    @Suppress("UNCHECKED_CAST")
    internal fun getConfigProperties() = this::class.memberProperties.mapNotNull {
        val prop = it as KProperty1<Konfig, *>
        prop.isAccessible = true
        val delegate = prop.getDelegate(this) as? DelegatedProperty<*> ?: return@mapNotNull null
        prop.isAccessible = false

        return@mapNotNull prop to delegate
    }
}

/**
 * Creates a required config property.
 *
 * @param generate what to set this value as when generating a new config
 */
@OptIn(InternalSerializationApi::class)
inline fun <reified T: Any> Konfig.required(generate: T): DelegatedProperty<T> {
    return RequiredProperty(T::class.serializer(), generate)
}
