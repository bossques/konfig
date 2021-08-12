package me.qbosst.konfig

import me.qbosst.konfig.engine.SerializationEngine

open class KonfigObject(override val parent: Konfigurable): Konfigurable() {

    final override val engine: SerializationEngine<*, *> get() = parent.engine

}