package me.qbosst.konfig.properties

import kotlinx.serialization.KSerializer
import me.qbosst.konfig.Konfig
import me.qbosst.konfig.engine.SerializationEngine
import me.qbosst.konfig.util.getSerialName
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

class DelegatedProvider<ARG: Any, ELEMENT: Any, DELEGATE: Any>(
    val serializer: KSerializer<ARG>,
    val default: ARG?,
    val createInstance: () -> DELEGATE
): PropertyDelegateProvider<Konfig<ELEMENT>, DELEGATE> {
    override fun provideDelegate(thisRef: Konfig<ELEMENT>, property: KProperty<*>): DELEGATE {
        thisRef.map[property.getSerialName()] = encodeDefaultToElement(thisRef.serializationEngine)

        return createInstance()
    }

    private fun encodeDefaultToElement(engine: SerializationEngine<*, ELEMENT>): ELEMENT = if(default == null) {
        engine.elementNull
    } else {
        engine.encodeToElement(serializer, default)
    }
}