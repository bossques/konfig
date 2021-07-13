package me.qbosst.konfig

import kotlin.reflect.KClass

open class ConfigException: IllegalArgumentException {
    /**
     * Creates an instance of [ConfigException] without any details.
     */
    constructor()

    /**
     * Creates an instance of [ConfigException] with the specified detail [message].
     */
    constructor(message: String?) : super(message)

    /**
     * Creates an instance of [ConfigException] with the specified detail [message], and the given [cause].
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    /**
     * Creates an instance of [ConfigException] with the specified [cause].
     */
    constructor(cause: Throwable?) : super(cause)
}

class ConfigPropertiesMissingException(val properties: List<String>): ConfigException(
    message = "The following config properties are required, but are missing: $properties"
)

class ConfigPropertyMissingException(val property: String): ConfigException(
    message = "Config value '$property' is not present"
)

class DefaultNotRegistered(val kClass: KClass<out Any>): ConfigException(
    message = "A default value for ${kClass.simpleName} has not been registered"
)