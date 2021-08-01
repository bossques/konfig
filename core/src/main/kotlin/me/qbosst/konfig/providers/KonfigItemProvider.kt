package me.qbosst.konfig.providers

import kotlinx.serialization.KSerializer
import me.qbosst.konfig.Konfigurable
import me.qbosst.konfig.KonfigItem
import me.qbosst.konfig.util.getSerialName
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

class KonfigItemProvider<ARG: Any, ARG_NULL: ARG?, DELEGATE>(
    val default: ARG_NULL,
    val serializer: KSerializer<ARG>,
    val delegateGetter: (KonfigItem<ARG, ARG_NULL>) -> DELEGATE
): PropertyDelegateProvider<Konfigurable, DELEGATE> {

    override fun provideDelegate(thisRef: Konfigurable, property: KProperty<*>): DELEGATE {
        val name = property.getSerialName()

        val item = KonfigItem<ARG, ARG_NULL>(name, default, serializer)
        thisRef.items[item.name] = item

        return delegateGetter(item)
    }

}