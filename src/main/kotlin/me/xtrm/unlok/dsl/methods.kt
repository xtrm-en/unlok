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

fun <T> method(
    owner: Class<*>,
    fieldName: String = "",
    methodDesc: String = "",
    ownerInstance: Any? = null,
): MethodDelegate<T> =
    method(owner.name, fieldName, methodDesc, ownerInstance)

fun <T> method(
    owner: KClass<*>,
    fieldName: String = "",
    methodDesc: String = "",
    ownerInstance: Any? = null,
): MethodDelegate<T> =
    method(owner.java.name, fieldName, methodDesc, ownerInstance)
