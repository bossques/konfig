package me.qbosst.konfig.util

import kotlinx.serialization.SerialName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

internal fun KProperty<*>.getSerialName() = findAnnotation<SerialName>()?.value ?: name

internal fun KClass<*>.getSerialName() = findAnnotation<SerialName>()?.value ?: simpleName ?: java.name