package me.qbosst.konfig

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