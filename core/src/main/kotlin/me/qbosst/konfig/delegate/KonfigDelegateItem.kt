package me.qbosst.konfig.delegate

import me.qbosst.konfig.KonfigItem
import me.qbosst.konfig.Konfigurable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class KonfigDelegateItem<ARG: Any, ARG_NULL: ARG?>: ReadWriteProperty<Konfigurable, ARG_NULL> {
    abstract val item: KonfigItem<ARG, ARG_NULL>

    override fun getValue(thisRef: Konfigurable, property: KProperty<*>): ARG_NULL {
        return item.decodeFromElement(thisRef.engine)
    }

    override fun setValue(thisRef: Konfigurable, property: KProperty<*>, value: ARG_NULL) {
        item.encodeToElement(thisRef.engine, value)
    }
}

class RequiredKonfigDelegateItem<ARG: Any>(override val item: KonfigItem<ARG, ARG>) : KonfigDelegateItem<ARG, ARG>()

class OptionalKonfigDelegateItem<ARG: Any, ARG_NULL: ARG?>(override val item: KonfigItem<ARG, ARG_NULL>) : KonfigDelegateItem<ARG, ARG_NULL>()

class DefaultingKonfigDelegateItem<ARG: Any>(override val item: KonfigItem<ARG, ARG>) : KonfigDelegateItem<ARG, ARG>()



