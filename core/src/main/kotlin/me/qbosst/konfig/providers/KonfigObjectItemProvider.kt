package me.qbosst.konfig.providers

import me.qbosst.konfig.KonfigObject
import me.qbosst.konfig.KonfigObjectItem
import me.qbosst.konfig.Konfigurable
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmName

class KonfigObjectItemProvider<T: KonfigObject, DELEGATE>(
    val kClass: KClass<T>,
    val instance: T,
    val delegateGetter: (KonfigObjectItem<T>) -> DELEGATE
): PropertyDelegateProvider<Konfigurable, DELEGATE> {

    override fun provideDelegate(thisRef: Konfigurable, property: KProperty<*>): DELEGATE {
        val name = kClass.jvmName

        val item = KonfigObjectItem(name, instance)
        thisRef.items[item.name] = item

        return delegateGetter(item)
    }
}