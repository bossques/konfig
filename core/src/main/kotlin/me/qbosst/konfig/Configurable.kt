package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.qbosst.konfig.engine.SerializationEngine
import me.qbosst.konfig.util.ConfigDefaults
import me.qbosst.konfig.util.getSerialName
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

abstract class Configurable<E: Any> {
    abstract val serializationEngine: SerializationEngine<*, E>

    internal lateinit var map: MutableMap<String, E>

    @Suppress("UNCHECKED_CAST")
    internal fun getConfigProperties() = this::class.memberProperties.mapNotNull {
        val prop = it as KProperty1<Configurable<E>, *>
        prop.isAccessible = true
        val delegate = prop.getDelegate(this) as? DelegatedConfigProperty<out Any?, out Any, *, E, SerializationEngine<*, E>> ?: return@mapNotNull null
        prop.isAccessible = false

        return@mapNotNull prop to delegate
    }

    internal fun getProperty(name: String) = this::class.memberProperties.firstOrNull { it.getSerialName() == name }
}

inline fun <reified T: Any, E: Any> Configurable<E>.required(
    default: T = ConfigDefaults[T::class],
    serializer: KSerializer<T> = serializer()
): RequiredConfigProperty<T, *, E, *> = RequiredConfigProperty(default, serializer, serializationEngine)

@JvmName("requiredElement")
inline fun <reified T: Any, E: Any> Configurable<E>.required(
    default: E,
    serializer: KSerializer<T> = serializer()
): RequiredConfigProperty<T, *, E, *> = RequiredConfigProperty(serializer, default, serializationEngine)

inline fun <reified T: Any, E: Any> Configurable<E>.defaulting(
    default: T,
    serializer: KSerializer<T> = serializer()
): DefaultingConfigProperty<T, *, E, *> = DefaultingConfigProperty(default, serializer, serializationEngine)

@JvmName("defaultingElement")
inline fun <reified T: Any, E: Any> Configurable<E>.defaulting(
    default: E,
    serializer: KSerializer<T> = serializer()
): DefaultingConfigProperty<T, *, E, *> = DefaultingConfigProperty(serializer, default, serializationEngine)

inline fun <reified T: Any, E: Any> Configurable<E>.optional(
    default: T? = null,
    serializer: KSerializer<T> = serializer()
): OptionalConfigProperty<T?, T, *, E, *> = OptionalConfigProperty(default, serializer, serializationEngine)

@JvmName("optionalElement")
inline fun <reified T: Any, E: Any> Configurable<E>.optional(
    default: E,
    serializer: KSerializer<T> = serializer()
): OptionalConfigProperty<T?, T, *, E, *> = OptionalConfigProperty(serializer, default, serializationEngine)

