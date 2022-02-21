package me.xtrm.unlok.utils

import java.lang.reflect.Field
import java.lang.reflect.Modifier

object AccessorUtils {

    @JvmStatic
    fun setupFinalField(holderClass: Class<*>, fieldName: String): Field =
        holderClass.getDeclaredField(fieldName).also { field ->
            field.isAccessible = true
            val modifiersField = Field::class.java.getDeclaredField("modifiers")
                .also { it.isAccessible = true }
            modifiersField.setInt(field, modifiersField.getInt(field).and(Modifier.FINAL.inv()))
        }

    @JvmStatic
    fun setFinalField(finalField: Field, instance: Any?, value: Any?) =
        finalField.set(instance, value)
}
