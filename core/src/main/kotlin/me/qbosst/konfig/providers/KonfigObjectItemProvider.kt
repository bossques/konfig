package me.qbosst.konfig.providers

import me.qbosst.konfig.KonfigObject
import me.qbosst.konfig.KonfigObjectItem
import me.qbosst.konfig.Konfigurable
import me.qbosst.konfig.util.getSerialName
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class KonfigObjectItemProvider<T: KonfigObject, DELEGATE>(
    val kClass: KClass<T>,
    val instance: T,
    val delegateGetter: (KonfigObjectItem<T>) -> DELEGATE
): PropertyDelegateProvider<Konfigurable, DELEGATE> {

    override fun provideDelegate(thisRef: Konfigurable, property: KProperty<*>): DELEGATE {
        val name = property.getSerialName()

        val item = KonfigObjectItem(name, instance)
        thisRef.items[item.name] = item

        return delegateGetter(item)
    }
}