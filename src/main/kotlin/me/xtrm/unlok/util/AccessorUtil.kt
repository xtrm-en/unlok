package me.xtrm.unlok.util

import dev.xdark.deencapsulation.Deencapsulation
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
internal object AccessorUtil {

    /**
     * Reference to the [Class.getDeclaredFields0] native method,
     * used to bypass the Reflection field filter.
     */
    private val m_getDeclaredFields0 by lazy {
        Class::class.java.getDeclaredMethod("getDeclaredFields0", Boolean::class.java)
            .also { it.isAccessible = true }
    }

    init {
        try {
            Deencapsulation.deencapsulate(Class::class.java)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    /**
     * Sets the given final field up to be able to access it.
     *
     * @param holderClass The class that holds the wanted field.
     * @param fieldName The name of the wanted field.
     *
     * @return The set-up field.
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun setupFinalField(holderClass: Class<*>, fieldName: String): Field =
        holderClass.getDeclaredField(fieldName).apply {
            isAccessible = true
            val declaredFields = m_getDeclaredFields0.invoke(
                Field::class.java,
                false
            ) as Array<Field>

            declaredFields
                .first { it.name.equals("modifiers") }
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
