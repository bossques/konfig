package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.qbosst.konfig.engine.SerializationEngine
import me.qbosst.konfig.delegate.DefaultingKonfigDelegateItem
import me.qbosst.konfig.delegate.OptionalKonfigDelegateItem
import me.qbosst.konfig.delegate.RequiredKonfigDelegateItem
import me.qbosst.konfig.delegate.RequiredKonfigDelegateObjectItem
import me.qbosst.konfig.providers.KonfigItemProvider
import me.qbosst.konfig.providers.KonfigObjectItemProvider
import kotlin.reflect.KClass

abstract class Konfigurable {
    abstract val parent: Konfigurable?
    abstract val engine: SerializationEngine<*, *>

    internal var items: MutableMap<String, KonfigProperty<out Any?>> = mutableMapOf()

    inline fun <reified T: Any> requiredItem(
        default: T = Konfigurable[T::class],
        serializer: KSerializer<T> = serializer()
    ) = KonfigItemProvider(default, serializer, ::RequiredKonfigDelegateItem)

    inline fun <reified T: Any> optionalItem(
        default: T?,
        serializer: KSerializer<T> = serializer()
    ) = KonfigItemProvider(default, serializer, ::OptionalKonfigDelegateItem)

    inline fun <reified T: Any> defaultingItem(
        default: T,
        serializer: KSerializer<T> = serializer()
    ) = KonfigItemProvider(default, serializer, ::DefaultingKonfigDelegateItem)

    inline fun <reified T: KonfigObject> requiredObject(
        builder: (Konfigurable) -> T
    ) = KonfigObjectItemProvider(T::class, builder(this), ::RequiredKonfigDelegateObjectItem)

    companion object {
        private val defaults: MutableMap<KClass<out Any>, Any> = mutableMapOf(
            String::class to "",
            Char::class to ' ',
            Short::class to 0,
            Int::class to 0,
            Long::class to 0,
            Float::class to 0.0f,
            Double::class to 0.0,
            Collection::class to emptyList<Any?>(),
            List::class to emptyList<Any?>(),
            Set::class to emptySet<Any?>(),
            Array::class to emptyArray<Any?>(),
            Map::class to emptyMap<Any?, Any?>(),
        )

        fun <T: Any> register(kClass: KClass<T>, default: T) {
            defaults[kClass] = default
        }

        @Suppress("UNCHECKED_CAST")
        fun <T: Any> getOrNull(kClass: KClass<T>): T? = defaults[kClass] as? T

        operator fun <T: Any> get(kClass: KClass<T>): T = getOrNull(kClass)
            ?: throw KonfigException("A default value for ${kClass::simpleName} has not been registered")
    }
}
