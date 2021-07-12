package me.qbosst.konfig.util

import kotlinx.serialization.SerialName
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

internal fun KProperty<*>.getSerialName() = findAnnotation<SerialName>()?.value ?: name