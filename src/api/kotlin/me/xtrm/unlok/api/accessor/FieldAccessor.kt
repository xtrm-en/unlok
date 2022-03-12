package me.xtrm.unlok.api.accessor

/**
 * Interface proxy that grants access to a field.
 *
 * @author xtrm
 * @since 0.0.1
 */
interface FieldAccessor<T> {
    /**
     * @return the field's value
     */
    fun get(): T?

    /**
     * @param value the new field's value
     */
    fun set(value: T?)
}
