package me.xtrm.unlok.dsl

import me.xtrm.unlok.Unlok
import me.xtrm.unlok.delegate.MethodDelegate
import kotlin.reflect.KClass

fun <T> method(
    owner: String,
    methodName: String = "",
    methodDesc: String = "",
    ownerInstance: Any? = null,
): MethodDelegate<T> =
    Unlok.method(owner, methodName, methodDesc, ownerInstance)

fun <T> Class<*>.method(
    fieldName: String = "",
    methodDesc: String = "",
    ownerInstance: Any? = null,
): MethodDelegate<T> =
    method(this.name, fieldName, methodDesc, ownerInstance)

fun <T> KClass<*>.method(
    fieldName: String = "",
    methodDesc: String = "",
    ownerInstance: Any? = null,
): MethodDelegate<T> =
    this.java.method(fieldName, methodDesc, ownerInstance)
