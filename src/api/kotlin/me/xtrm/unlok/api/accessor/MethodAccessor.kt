package me.xtrm.unlok.api.accessor

/**
 * Interface proxy that grants access to a method.
 *
 * @author xtrm
 * @since 0.2.0
 */
interface MethodAccessor<T> {
    /**
     * @param arguments the method's arguments
     *
     * @return the method's return value
     */
    fun invoke(vararg arguments: Any?): T?
}
