package me.xtrm.unlok.dsl

import me.xtrm.unlok.Unlok
import me.xtrm.unlok.delegate.FieldDelegate
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
