package me.xtrm.unlok.api.accessor

/**
 * Interface proxy that grants access to fields.
 *
 * @author xtrm-en
 * @since 0.0.1
 */
interface FieldAccessor<T> {
    /**
     * @return The field's value
     */
    fun get(): T?

    /**
     * @param value The field's value
     */
    fun set(value: T?)
}
