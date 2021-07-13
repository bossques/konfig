package me.qbosst.konfig.util

import me.qbosst.konfig.DefaultNotRegistered
import kotlin.reflect.KClass

object ConfigDefaults {
    private val defaults: MutableMap<KClass<out Any>, Any> = mutableMapOf()

    fun <T : Any> register(kClass: KClass<T>, default: T) {
        defaults[kClass] = default
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T: Any> get(kClass: KClass<T>) = getOrNull(kClass) ?: throw DefaultNotRegistered(kClass)

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> getOrNull(kClass: KClass<T>): T? = if(kClass in defaults) defaults[kClass] as T else null

    fun registerDefaults() {
        defaults[String::class] = ""
        defaults[Char::class] = ' '
        defaults[Short::class] = 0
        defaults[Int::class] = 0
        defaults[Long::class] = 0L
        defaults[Float::class] = 0.0f
        defaults[Double::class] = 0.0
    }
}