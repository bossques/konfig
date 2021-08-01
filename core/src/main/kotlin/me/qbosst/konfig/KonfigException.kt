package me.qbosst.konfig

open class KonfigException: IllegalArgumentException {
    /**
     * Creates an instance of [KonfigException] without any details.
     */
    constructor()

    /**
     * Creates an instance of [KonfigException] with the specified detail [message].
     */
    constructor(message: String?) : super(message)

    /**
     * Creates an instance of [KonfigException] with the specified detail [message], and the given [cause].
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    /**
     * Creates an instance of [KonfigException] with the specified [cause].
     */
    constructor(cause: Throwable?) : super(cause)
}