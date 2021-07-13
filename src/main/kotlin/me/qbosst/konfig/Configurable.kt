@file:OptIn(InternalSerializationApi::class)

package me.qbosst.konfig

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import me.qbosst.konfig.util.ConfigDefaults
import me.qbosst.konfig.util.getSerialName
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

open class Configurable {
    internal lateinit var map: MutableMap<String, JsonElement>

    @Suppress("UNCHECKED_CAST")
    internal fun getConfigProperties() = this::class.memberProperties.mapNotNull {
        val prop = it as KProperty1<Configurable, *>
        prop.isAccessible = true
        val delegate = prop.getDelegate(this) as? DelegatedConfigProperty<out Any?, out Any> ?: return@mapNotNull null
        prop.isAccessible = false

        return@mapNotNull prop to delegate
    }

    internal fun getProperty(name: String) = this::class.memberProperties.firstOrNull { it.getSerialName() == name }
}

inline fun <reified T: Any> Configurable.required(
    default: T = ConfigDefaults[T::class],
    serializer: KSerializer<T> = T::class.serializer()
): RequiredConfigProperty<T> = RequiredConfigProperty(serializer, default)

inline fun <reified T: Any> Configurable.required(
    default: JsonElement,
    serializer: KSerializer<T> = T::class.serializer()
): RequiredConfigProperty<T> = RequiredConfigProperty(serializer, default)

inline fun <reified T: Any> Configurable.defaulting(
    default: T,
    serializer: KSerializer<T> = T::class.serializer()
): DefaultingConfigProperty<T> = DefaultingConfigProperty(serializer, default)

inline fun <reified T: Any> Configurable.defaulting(
    default: JsonElement,
    serializer: KSerializer<T> = T::class.serializer()
): DefaultingConfigProperty<T> = DefaultingConfigProperty(serializer, default)

inline fun <reified T: Any> Configurable.optional(
    default: T? = null,
    serializer: KSerializer<T> = T::class.serializer()
): OptionalConfigProperty<T?, T> = OptionalConfigProperty(serializer, default)

inline fun <reified T: Any> Configurable.optional(
    default: JsonElement,
    serializer: KSerializer<T> = T::class.serializer()
): OptionalConfigProperty<T?, T> = OptionalConfigProperty(serializer, default)

