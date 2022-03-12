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

fun <T> Class<*>.field(
    fieldName: String = "",
    ownerInstance: Any? = null,
): FieldDelegate<T> =
    field(this.name, fieldName, ownerInstance)

fun <T> KClass<*>.field(
    fieldName: String = "",
    ownerInstance: Any? = null,
): FieldDelegate<T> =
    this.java.field(fieldName, ownerInstance)
