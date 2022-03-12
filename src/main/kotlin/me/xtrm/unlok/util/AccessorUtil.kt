package me.xtrm.unlok.util

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Methods used in reflection while accessing stuff with
 * [me.xtrm.unlok.accessor.AccessorBuilder].
 *
 * @author xtrm
 * @since 0.0.1
 */
@Suppress("unused")
object AccessorUtil {
    /**
     * Sets the given final field up to be able to access it.
     *
     * @param holderClass The class that holds the wanted field.
     * @param fieldName The name of the wanted field.
     *
     * @return The set-up field.
     */
    @JvmStatic
    fun setupFinalField(holderClass: Class<*>, fieldName: String): Field =
        holderClass.getDeclaredField(fieldName).apply {
            isAccessible = true
            Field::class.java
                .getDeclaredField("modifiers")
                .run {
                    isAccessible = true
                    setInt(
                        this@apply,
                        this.getInt(this@apply).and(Modifier.FINAL.inv())
                    )
                }
        }

    /**
     * Sets the given final field value to the given one, using the instance if
     * the field is static.
     *
     * @param finalField The final field to set the value of.
     * @param instance The instance of the object that holds the field, if the
     *                 field is static.
     * @param value The new value.
     */
    @JvmStatic
    fun setFinalField(finalField: Field, instance: Any?, value: Any?) =
        finalField.set(instance, value)
}
