package me.xtrm.unlok.accessor

/**
 * @author xtrm-en
 * @since 0.0.1
 */
interface FieldAccessor<T> {

    fun get(): T?

    fun set(value: T?)

}