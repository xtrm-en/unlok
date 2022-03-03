package me.xtrm.unlok.dsl

import me.xtrm.unlok.Unlok
import me.xtrm.unlok.delegate.FieldDelegate
import me.xtrm.unlok.delegate.MethodDelegate
import kotlin.reflect.KClass

fun <T> field(
    owner: String,
    fieldName: String = "",
    ownerInstance: Any? = null,
): FieldDelegate<T> =
    Unlok.field(owner, fieldName, ownerInstance)

fun <T> field(
    owner: Class<*>,
    fieldName: String = "",
    ownerInstance: Any? = null,
): FieldDelegate<T> =
    field(owner.name, fieldName, ownerInstance)

fun <T> field(
    owner: KClass<*>,
    fieldName: String = "",
    ownerInstance: Any? = null,
): FieldDelegate<T> =
    field(owner.java.name, fieldName, ownerInstance)

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
