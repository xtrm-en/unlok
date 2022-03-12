package me.xtrm.unlok.dsl

import me.xtrm.unlok.Unlok
import me.xtrm.unlok.delegate.MethodDelegate
import kotlin.reflect.KClass

/**
 * Provides a [MethodDelegate] by delegation.
 *
 * @see Unlok.method
 */
fun <T> method(
    owner: String,
    methodName: String = "",
    methodDesc: String = "",
    ownerInstance: Any? = null,
): MethodDelegate<T> =
    Unlok.method(owner, methodName, methodDesc, ownerInstance)

/**
 * Provides a [MethodDelegate] by delegation, on a standard Java [Class].
 *
 * @see Unlok.method
 */
fun <T> Class<*>.method(
    fieldName: String = "",
    methodDesc: String = "",
    ownerInstance: Any? = null,
): MethodDelegate<T> =
    method(this.name, fieldName, methodDesc, ownerInstance)

/**
 * Provides a [MethodDelegate] by delegation, on a Kotlin [KClass].
 *
 * @see Unlok.method
 */
fun <T> KClass<*>.method(
    fieldName: String = "",
    methodDesc: String = "",
    ownerInstance: Any? = null,
): MethodDelegate<T> =
    this.java.method(fieldName, methodDesc, ownerInstance)
