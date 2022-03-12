package me.xtrm.unlok.util

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * @author xtrm-en
 * @since 0.0.1
 */
// because we use it with reflections (see [AccessorBuilder]):
@Suppress("unused")
object AccessorUtil {
    @JvmStatic
    fun setupFinalField(holderClass: Class<*>, fieldName: String): Field =
        holderClass.getDeclaredField(fieldName).also {
            it.isAccessible = true

            val modifiersField = Field::class.java.getDeclaredField("modifiers")
                .also { field -> field.isAccessible = true }

            modifiersField.setInt(it, modifiersField.getInt(it).and(Modifier.FINAL.inv()))
        }

    @JvmStatic
    fun setFinalField(finalField: Field, instance: Any?, value: Any?) =
        finalField.set(instance, value)
}
