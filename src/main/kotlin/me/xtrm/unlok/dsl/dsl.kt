package me.xtrm.unlok.dsl

import me.xtrm.unlok.Unlok
import me.xtrm.unlok.delegate.FieldDelegate

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
): FieldDelegate<T> {
    return field(owner.name, fieldName, ownerInstance)
}
