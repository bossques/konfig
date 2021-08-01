package me.qbosst.konfig.delegate

import me.qbosst.konfig.KonfigObject
import me.qbosst.konfig.KonfigObjectItem
import me.qbosst.konfig.Konfigurable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class KonfigDelegateObjectItem<T: KonfigObject>: ReadWriteProperty<Konfigurable, T> {
    abstract val item: KonfigObjectItem<T>

    override fun getValue(thisRef: Konfigurable, property: KProperty<*>): T {
        return item.kObj
    }

    override fun setValue(thisRef: Konfigurable, property: KProperty<*>, value: T) {
        item.kObj = value
    }
}

class RequiredKonfigDelegateObjectItem<T: KonfigObject>(override val item: KonfigObjectItem<T>) : KonfigDelegateObjectItem<T>()