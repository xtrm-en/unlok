package me.xtrm.unlok.dsl

import me.xtrm.unlok.Unlok
import me.xtrm.unlok.delegate.FieldDelegate
import kotlin.reflect.KClass

/**
 * Provides a [FieldDelegate] by delegation.
 *
 * @see Unlok.field
 */
fun <T> field(
    ownerClassName: String,
    fieldName: String = "",
    ownerInstance: Any? = null,
): FieldDelegate<T> =
    Unlok.field(ownerClassName, fieldName, ownerInstance)

/**
 * Provides a [FieldDelegate] by delegation, on a standard Java [Class].
 *
 * @see Unlok.field
 */
fun <T> Class<*>.field(
    fieldName: String = "",
    ownerInstance: Any? = null,
): FieldDelegate<T> =
    field(this.name, fieldName, ownerInstance)

/**
 * Provides a [FieldDelegate] by delegation, on a Kotlin [KClass].
 *
 * @see Unlok.field
 */
fun <T> KClass<*>.field(
    fieldName: String = "",
    ownerInstance: Any? = null,
): FieldDelegate<T> =
    this.java.field(fieldName, ownerInstance)
